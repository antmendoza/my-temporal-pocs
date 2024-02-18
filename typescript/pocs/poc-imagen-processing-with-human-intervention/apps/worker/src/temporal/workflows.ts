import {
  proxyActivities,
  setHandler,
  condition,
  startChild,
  WorkflowExecutionAlreadyStartedError,
  Workflow,
  ApplicationFailure,
  log,
  continueAsNew,
} from '@temporalio/workflow';
import type { ActivitiesService } from '../activities/activities.service';
import { defineSignal } from '@temporalio/workflow';
import { ChildWorkflowHandle } from '@temporalio/workflow/src/workflow-handle';
import { ProcessCompletedResponse } from '@app/shared';

const { uploadImage } = proxyActivities<ActivitiesService>({
  startToCloseTimeout: '5 seconds',
});

const { sendImageToProcess1, sendImageToProcess2 } = proxyActivities<ActivitiesService>({
  startToCloseTimeout: '2 seconds',
});

interface ImagesRequest {
  images: Image[];
  pageSize: number;
}

interface Image {
  name: string;
}

export async function processImages(images: ImagesRequest): Promise<void> {
  const childWorkflowHandles: ChildWorkflowHandle<Workflow>[] = [];

  for (const [index, image] of images.images.entries()) {
    const childWorkflowHandle = await startChild(processImage, {
      //Execution already started will fail the workflow execution if we don't catch the exception
      workflowId: 'processImage-' + image.name,
      args: [image],
    }).catch((e) => {
      if (e instanceof WorkflowExecutionAlreadyStartedError) {
        //rethrow if you want to fail the parent workflow
        return null;
      }
      throw e;
    });

    // child already started won't be added
    if (childWorkflowHandle) {
      childWorkflowHandles.push(childWorkflowHandle);
    }

    // Break the loop
    if (index + 1 >= images.pageSize) {
      break;
    }
  }

  // Wait for all child workflows to complete
  // or we can start the child workflows in abandon mode and don't wait
  await Promise.all(childWorkflowHandles);
  for (const childWorkflowHandle of childWorkflowHandles) {
    await childWorkflowHandle.result().catch((e) => {
      log.info('Error child workflow ' + childWorkflowHandle.workflowId, e);
      // Child workflow fails, if we don't handle the error it will fail the parent workflow,
      // with the current policy running child workflows will be terminated
    });
  }
  //For activities is different, before continue as new, ensure all activities have completed

  //Continue as new if there are elements to process
  const nextImages = images.images.slice(images.pageSize);
  if (nextImages && nextImages.length > 0) {
    const args: ImagesRequest = {
      images: nextImages,
      pageSize: images.pageSize,
    };

    await continueAsNew(args);
  }

  return null;
}

export const processCompleted = defineSignal<[ProcessCompletedResponse]>('processCompleted');

export async function processImage(image: Image): Promise<void> {
  let allStepsCompleted = false;

  //Failing child workflows intentionally
  const imageName = image.name;
  if (imageName.endsWith('2')) {
    throw ApplicationFailure.create({ nonRetryable: true, message: 'Workflow intentionally failed' });
  }

  const pending_request: ProcessCompletedResponse[] = [];

  setHandler(processCompleted, (processCompleted: ProcessCompletedResponse) => {
    pending_request.push(processCompleted);
  });

  await uploadImage(imageName).catch((e) => {
    // This can fail, rethrow to fail workflow
    log.error(e);
  });

  await Promise.all([sendImageToProcess1(imageName)]);

  while (!allStepsCompleted) {
    await condition(() => pending_request.length > 0);

    const request = pending_request.pop();

    if (request.callerActivity == 'sendImageToProcess1') {
      await Promise.all([sendImageToProcess2(imageName)]);
    }

    if (request.callerActivity == 'sendImageToProcess2') {
      allStepsCompleted = true;
    }
  }

  return;
}

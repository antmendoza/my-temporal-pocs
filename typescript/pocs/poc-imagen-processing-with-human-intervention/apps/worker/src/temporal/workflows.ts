import {
  proxyActivities,
  setHandler,
  condition,
  startChild,
  sleep,
  WorkflowExecutionAlreadyStartedError,
  Workflow,
  ApplicationFailure,
  log,
} from '@temporalio/workflow';
import type { ActivitiesService, UploadImageResponse } from '../activities/activities.service';
import { defineSignal } from '@temporalio/workflow';
import { ChildWorkflowHandle } from '@temporalio/workflow/src/workflow-handle';
import { ProcessCompletedResponse } from '@app/shared';

const { uploadImage, sendImageToProcess1, sendImageToProcess2 } = proxyActivities<ActivitiesService>({
  startToCloseTimeout: '5 seconds',
});

interface Images {
  images: Image[];
}

interface Image {
  name: string;
}

export async function processImages(images?: Images): Promise<void> {
  const childWorkflowHandles: ChildWorkflowHandle<Workflow>[] = [];

  for (const image of images.images) {
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
  }

  await Promise.all(childWorkflowHandles);

  for (const childWorkflowHandle of childWorkflowHandles) {
    await childWorkflowHandle.result().catch((e) => {
      log.info('Error child workflow ' + childWorkflowHandle.workflowId, e);
      // Child workflow fails, if we don't handle the error it will fail the parent workflow,
      // with the current policy running child workflows will be terminated
    });
  }

  //For activities is different, before continue as new, ensure all activities have completed

  return null;
}

export const processCompleted = defineSignal<[ProcessCompletedResponse]>('processCompleted');

export async function processImage(image: Image): Promise<void> {
  //Failing some child workflows
  const imageName = image.name;
  if (imageName.endsWith('2')) {
    throw ApplicationFailure.create({ nonRetryable: true, message: 'Workflow intentionally failed' });
  }

  const pending_request: ProcessCompletedResponse[] = [];

  setHandler(processCompleted, (processCompleted: ProcessCompletedResponse) => {
    console.log('request in sicnal   ' + JSON.stringify(processCompleted));

    pending_request.push(processCompleted);
  });

  await uploadImage(imageName).catch((e) => {
    // This can fail, rethrow to fail workflow
    log.error(e);
  });

  await Promise.all([sendImageToProcess1(imageName)]);

  let completed = false;
  while (!completed) {
    await condition(() => pending_request.length > 0);

    const request = pending_request.pop();

    console.log('request   ' + JSON.stringify(request));

    if (request.callerActivity == 'sendImageToProcess1') {
      await Promise.all([sendImageToProcess2(imageName)]);
      continue;
    }

    if (request.callerActivity == 'sendImageToProcess2') {
      completed = true;
      continue;
    }
  }

  return;
}

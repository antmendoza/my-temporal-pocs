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

const { uploadImage, sendImageAndForget_1, sendImageAndForget_2 } = proxyActivities<ActivitiesService>({
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
      } else {
        throw e;
      }
      return null;
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

interface StepCompleted {
  image: string;
  stepId: string;
}

export const stepCompleted = defineSignal<[StepCompleted]>('step1Completed');

export async function processImage(image: Image): Promise<void> {
  //Failing some child workflows
  if (image.name.endsWith('2')) {
    throw ApplicationFailure.create({ nonRetryable: true, message: 'Workflow intentionally failed' });
  }

  const pending_request: StepCompleted[] = [];

  setHandler(stepCompleted, (step: StepCompleted) => {
    pending_request.push(step);
  });

  await uploadImage(image.name).catch((e) => {
    // This can fail, what to do?
    log.error(e);
  });

  let completed = true;
  while (!completed) {
    condition(() => pending_request.length > 0);

    const request = pending_request.pop();

    if (request.stepId == 'step1') {
      continue;
    }

    if (request.stepId == 'step2') {
      completed = true;

      continue;
    }
  }
  return;
}

import { ActivitiesService } from '../activities/activities.service';
import { Runtime, Worker } from '@temporalio/worker';
import { taskQueue } from '@app/shared';

export const processImagesWorkerProviders = [
  {
    provide: 'IMAGE_PROCESSOR_WORKER',
    inject: [ActivitiesService],
    useFactory: async (activitiesService: ActivitiesService) => {
      Runtime.install({
        telemetryOptions: {
          metrics: {
            prometheus: { bindAddress: '0.0.0.0:9464' },
          },
        },
      });

      const activities = {
        uploadImage: activitiesService.uploadImage.bind(activitiesService),
        sendImageToProcess1: activitiesService.sendImageToProcess1.bind(activitiesService),
        sendImageToProcess2: activitiesService.sendImageToProcess2.bind(activitiesService),
      };

      const worker = await Worker.create({
        workflowsPath: require.resolve('../temporal/workflows'),
        taskQueue,
        activities,
      });

      worker.run();
      console.log('Started worker!');

      return worker;
    },
  },
];

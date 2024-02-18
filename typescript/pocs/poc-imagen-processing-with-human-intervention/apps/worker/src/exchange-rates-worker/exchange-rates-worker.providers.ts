import { ActivitiesService } from '../activities/activities.service';
import { Worker } from '@temporalio/worker';
import { taskQueue } from '@app/shared';

export const exchangeRatesWorkerProviders = [
  {
    provide: 'EXCHANGE_RATES_WORKER',
    inject: [ActivitiesService],
    useFactory: async (activitiesService: ActivitiesService) => {
      const activities = {
        uploadImage: activitiesService.uploadImage.bind(activitiesService),
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

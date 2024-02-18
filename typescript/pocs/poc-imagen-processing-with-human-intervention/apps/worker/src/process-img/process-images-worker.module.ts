import { ActivitiesModule } from '../activities/activities.module';
import { Module } from '@nestjs/common';
import { processImagesWorkerProviders } from './process-images-worker.providers';
import { ProcessImagesWorkerService } from './process-images-worker.service';

@Module({
  imports: [ActivitiesModule],
  controllers: [],
  providers: [...processImagesWorkerProviders, ProcessImagesWorkerService],
})
export class ProcessImagesWorkerModule {}

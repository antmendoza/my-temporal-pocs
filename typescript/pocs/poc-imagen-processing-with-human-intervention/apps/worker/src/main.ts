import { NestFactory } from '@nestjs/core';
import { ProcessImagesWorkerModule } from './process-img/process-images-worker.module';

async function bootstrap() {
  const app = await NestFactory.create(ProcessImagesWorkerModule);
  await app.listen(3001);
}
bootstrap();

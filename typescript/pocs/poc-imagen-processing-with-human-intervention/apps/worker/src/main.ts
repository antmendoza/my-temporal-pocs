import { NestFactory } from '@nestjs/core';
import { ProcessImagesWorkerModule } from './exchange-rates-worker/process-images-worker.module';

async function bootstrap() {
  const app = await NestFactory.create(ProcessImagesWorkerModule);
  await app.listen(3001);
}
bootstrap();

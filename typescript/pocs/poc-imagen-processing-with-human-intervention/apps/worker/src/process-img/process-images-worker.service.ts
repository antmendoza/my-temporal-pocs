import { Injectable, Inject } from '@nestjs/common';

@Injectable()
export class ProcessImagesWorkerService {
  constructor(@Inject('IMAGE_PROCESSOR_WORKER') private worker) {}

  async close() {
    await this.worker.close();
  }
}

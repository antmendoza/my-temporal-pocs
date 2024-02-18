import { Injectable, Inject } from '@nestjs/common';

@Injectable()
export class ProcessImagesWorkerService {
  constructor(@Inject('EXCHANGE_RATES_WORKER') private worker) {}

  async close() {
    await this.worker.close();
  }
}

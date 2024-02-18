import { Injectable, Inject } from '@nestjs/common';
import { Client } from '@temporalio/client';

@Injectable()
export class ExchangeRatesClient {
  constructor(@Inject('WORKFLOW_CLIENT') private client: Client) {}

  async signalWorkflow(data: { workflowId: string; data: any }) {
    return this.client.workflow.getHandle(data.workflowId).signal(data.data);
  }
}

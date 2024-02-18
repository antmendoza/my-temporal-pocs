import { Injectable, Inject } from '@nestjs/common';
import { Client } from '@temporalio/client';
import { ProcessSignalResponse } from '@app/shared';

@Injectable()
export class TemporalWorkflowClient {
  constructor(@Inject('WORKFLOW_CLIENT') private client: Client) {}

  async signalWorkflow(signal: { workflowId: string; data: ProcessSignalResponse }) {
    return this.client.workflow.getHandle(signal.workflowId).signal(signal.workflowId, signal.data);
  }
}

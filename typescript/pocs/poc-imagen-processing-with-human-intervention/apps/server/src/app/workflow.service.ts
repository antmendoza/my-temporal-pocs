import { Injectable, Inject } from '@nestjs/common';
import { Client } from '@temporalio/client';
import { ProcessCompletedResponse } from '@app/shared';

@Injectable()
export class TemporalWorkflowClient {
  constructor(@Inject('WORKFLOW_CLIENT') private client: Client) {}

  async signalWorkflow(signal: { workflowId: string; data: ProcessCompletedResponse }) {
    const signalName = 'processCompleted';
    return this.client.workflow.getHandle(signal.workflowId).signal(signalName, signal.data);
  }
}

import { Body, Controller, Get, Param, Post, Put } from '@nestjs/common';
import { TemporalWorkflowClient } from './workflow.service';
import { ProcessRequest, UploadImageDto } from '@app/shared';

@Controller('images')
export class ServerController {
  constructor(private readonly temporalWorkflowClient: TemporalWorkflowClient) {}

  @Put('upload')
  async uploadImage(@Body() uploadImageDto: UploadImageDto): Promise<string> {
    const activityInfo = uploadImageDto.activityInfo;
    if (activityInfo.attempt === 1) {
      await new Promise((resolve) => setTimeout(resolve, activityInfo.startToCloseTimeoutMs + 1000));
    }

    return await new Promise((resolve) => {
      resolve('' + Math.random());
    });
  }

  @Put('executeProcess')
  async executeProcess(@Body() processRequest: ProcessRequest): Promise<string> {
    //Signal back the caller after 10 seconds
    new Promise(() => {
      setTimeout(() => {
        const activityInfo = processRequest.activityInfo;
        this.temporalWorkflowClient.signalWorkflow({
          workflowId: activityInfo.workflowExecution.workflowId,
          data: {
            callerActivity: activityInfo.activityType,
            processName: processRequest.processName,
          },
        });
      }, 3000);
    });

    return await new Promise((resolve) => {
      resolve('' + Math.random());
    });
  }
}

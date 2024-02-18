import { Module } from '@nestjs/common';
import { ServerController } from './server.controller';
import { serverProviders } from './server.providers';
import { TemporalWorkflowClient } from './workflow.service';

@Module({
  imports: [],
  controllers: [ServerController],
  providers: [...serverProviders, TemporalWorkflowClient],
})
export class ServerModule {}

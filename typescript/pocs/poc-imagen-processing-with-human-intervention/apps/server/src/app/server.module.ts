import { Module } from '@nestjs/common';
import { ServerController } from './server.controller';
import { serverProviders } from './server.providers';
import { ExchangeRatesClient } from './workflow.service';

@Module({
  imports: [],
  controllers: [ServerController],
  providers: [...serverProviders, ExchangeRatesClient],
})
export class ServerModule {}

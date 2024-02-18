import { Module } from '@nestjs/common';
import { ServerModule } from './app/server.module';
@Module({
  imports: [ServerModule],
  controllers: [],
  providers: [],
})
export class AppModule {}

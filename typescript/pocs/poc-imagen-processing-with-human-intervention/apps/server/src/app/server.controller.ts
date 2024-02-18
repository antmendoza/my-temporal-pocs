import { Body, Controller, Get, Param, Post, Put } from '@nestjs/common';
import { ExchangeRatesClient } from './workflow.service';
import { UploadImageDto } from '@app/shared';

@Controller('images')
export class ServerController {
  constructor(private readonly exchangeRatesClient: ExchangeRatesClient) {}

  @Put('upload')
  async uploadImage(@Body() uploadImageDto: UploadImageDto): Promise<string> {

    console.log("uploadImageDto" +  JSON.stringify(uploadImageDto))
    return await new Promise((resolve) => resolve('' + Math.random()));
  }


  @Get('')
  async upload(): Promise<string> {
    return await new Promise((resolve) => resolve('OK'));
  }
}

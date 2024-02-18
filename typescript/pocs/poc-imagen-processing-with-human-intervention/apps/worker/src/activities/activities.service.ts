import { HttpService } from '@nestjs/axios';
import { Injectable } from '@nestjs/common';

export interface UploadImageResponse {
  image: string;
  url: string;
}

@Injectable()
export class ActivitiesService {
  constructor(public readonly httpService: HttpService) {
  }
  async uploadImage(image: string): Promise<UploadImageResponse> {
    const ms = 4000;
    await new Promise((resolve) => setTimeout(resolve, ms));
    return {
      image,
      url: 'random-url' + image,
    };
  }

  async sendImageAndForget_1(image: string): Promise<void> {
    const ms = 400;
    await new Promise((resolve) => setTimeout(resolve, ms));
    return await new Promise((resolve) => resolve());
  }

  async sendImageAndForget_2(image: string): Promise<void> {
    const ms = 400;
    await new Promise((resolve) => setTimeout(resolve, ms));
    return await new Promise((resolve) => resolve());
  }
  
}

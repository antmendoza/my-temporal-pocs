import { HttpService } from '@nestjs/axios';
import { Injectable } from '@nestjs/common';
import { UploadImageDto } from '@app/shared';

export interface UploadImageResponse {
  image: string;
  url: string;
  token: string;
}

@Injectable()
export class ActivitiesService {
  constructor(public readonly httpService: HttpService) {}

  async uploadImage(image: string): Promise<UploadImageResponse> {
    const url = 'random-url' + image;
    const dto = { name: image, url } as UploadImageDto;
    const data = await this.httpService.axiosRef.put('http://localhost:3000/images/upload', dto, {
      timeout: 4000,
    });

    const ms = 4000;
    await new Promise((resolve) => setTimeout(resolve, ms));
    return {
      image,
      url,
      token: data.data,
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

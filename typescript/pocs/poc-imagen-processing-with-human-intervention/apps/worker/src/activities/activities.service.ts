import { HttpService } from '@nestjs/axios';
import { Injectable } from '@nestjs/common';
import { ProcessRequest, UploadImageDto } from '@app/shared';
import { activityInfo } from '@temporalio/activity';

export interface UploadImageResponse {
  image: string;
  url: string;
  token: string;
}

@Injectable()
export class ActivitiesService {
  constructor(public readonly httpService: HttpService) {}

  async uploadImage(image: string): Promise<UploadImageResponse> {
    const imgUrl = 'random-url' + image;
    const dto = {
      name: image,
      url: imgUrl,
      activityInfo: activityInfo(),
    } as UploadImageDto;

    const url = 'http://localhost:3000/images/upload';
    const data = await this.put(url, dto);

    return {
      image,
      url: imgUrl,
      token: data.data,
    };
  }

  async sendImageToProcess1(image: string): Promise<void> {
    await this.sendImageToProcess();
    return await new Promise((resolve) => resolve());
  }

  async sendImageToProcess2(image: string): Promise<void> {
    await this.sendImageToProcess();
    return await new Promise((resolve) => resolve());
  }

  private async sendImageToProcess() {
    const dto: ProcessRequest = {
      activityInfo: activityInfo(),
      processName: 'process-' + activityInfo().activityType,
    };

    const url = 'http://localhost:3000/images/executeProcess';

    await this.put(url, dto);
  }

  private async put(url: string, dto: any) {
    const config = {
      timeout: activityInfo().startToCloseTimeoutMs - activityInfo().startToCloseTimeoutMs * 0.1,
    };
    const data = await this.httpService.axiosRef.put(url, dto, config);
    return data;
  }
}

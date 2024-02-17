import { HttpService } from '@nestjs/axios';
import { Injectable } from '@nestjs/common';
import { ExchangeRates } from '@app/shared';

const url = 'https://cdn.moneyconvert.net/api/latest.json';

@Injectable()
export class ActivitiesService {
  constructor(public readonly httpService: HttpService) {}
  async getExchangeRates(): Promise<ExchangeRates> {
    try{
      const res = await this.httpService.axiosRef.get('http://localhost:8081', {
        timeout: 1000 * 4,
      });
      return res.data.rates;
    } catch (e: unknown) {
      console.log('error while uploading file', e);
      throw e;
    }
  }
}

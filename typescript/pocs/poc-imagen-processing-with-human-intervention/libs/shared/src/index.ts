import { defineQuery } from '@temporalio/workflow';
import { Info } from '@temporalio/activity';

export const taskQueue = 'image_processing-taskqueue';

interface ActivityContext {
  attempt: number;
  name: string;
}

export interface UploadImageDto {
  name: string;
  url: string;
  activityInfo: Info;
}

export interface ProcessRequest {
  activityInfo: Info;
}

export interface ProcessSignalResponse {
  callerActivity: string;
}

export type ExchangeRates = { [key: string]: number };

export const getExchangeRatesQuery = defineQuery<ExchangeRates | null>('getExchangeRates');

export type exchangeRatesWorkflowType = () => Promise<void>;

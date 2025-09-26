import { log, proxyActivities, workflowInfo } from '@temporalio/workflow';
import type * as activities from '../activities';
import { getContext, withContext } from '../context/workflow-interceptors';


const { extractCustomerNameFromContext } = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 minutes',
});

export async function sampleWorkflow(): Promise<void> {

  const customer = getContext().customer;
  console.log("Log from workflow with customer: " + customer );

  const clientContext = await extractCustomerNameFromContext();

  getContext().customer = "UpdatedCustomerInWorkflow";
  const afterUpdatingContextInWorkflow = await extractCustomerNameFromContext();

  const withContextValue=  await withContext({ customer: 'vip-123' }, async () => { return await extractCustomerNameFromContext(); })


  log.info( clientContext + " | " + afterUpdatingContextInWorkflow + " | " + withContextValue + " | " + await extractCustomerNameFromContext());
}

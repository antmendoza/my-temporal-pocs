import { log, proxyActivities, proxyLocalActivities, workflowInfo } from '@temporalio/workflow';
import type * as activities from '../activities';
import { getContext, withContext } from '../context/workflow-interceptors';


const { extractCustomerNameFromContext,
  throwError,
} = proxyActivities<typeof activities>({
  startToCloseTimeout: '5 minutes',
});




const {
  localToModifyCustomerName
} = proxyLocalActivities<typeof activities>({
  startToCloseTimeout: '5 seconds',
});

export async function sampleWorkflow(): Promise<void> {

  const customer = getContext().customer;
  console.log("Log from workflow with customer: " + customer );

  const clientContext = await extractCustomerNameFromContext();

  getContext().customer = "UpdatedCustomerInWorkflow";
  const afterUpdatingContextInWorkflow = await extractCustomerNameFromContext();

  const withContextValue=  await withContext({ customer: 'vip-123' }, async () => { return await extractCustomerNameFromContext(); })

  log.info( clientContext + " | " + afterUpdatingContextInWorkflow + " | " + withContextValue + " | " + await extractCustomerNameFromContext());

  await localToModifyCustomerName();

  log.info( clientContext + " | " + afterUpdatingContextInWorkflow + " | " + withContextValue + " | " + await extractCustomerNameFromContext());

  await throwError()


  log.info( clientContext + " | " + afterUpdatingContextInWorkflow + " | " + withContextValue + " | " + await extractCustomerNameFromContext());
}

import * as wf from "@temporalio/workflow";

export const isBlockedQuery = wf.defineQuery<boolean>('isBlocked');


//export { myWorkflow, workflowId } from './workflows-v1';
// export { myWorkflow, workflowId } from './workflows-v2';
 export { myWorkflow, workflowId } from './workflows-v3';
//export { myWorkflow, workflowId } from './workflows-vFinal';

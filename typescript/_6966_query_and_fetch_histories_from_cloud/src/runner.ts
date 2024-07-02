import {Client, AsyncWorkflowListIterable, HistoryAndWorkflowId} from '@temporalio/client';
import {getClient} from "./client";


async function run() {

    const client = await getClient();
    const query = "`WorkflowType`=\"IGreetingWorkflow\" AND `StartTime`>=\"2024-06-27T00:00:00.000Z\"";
    const result = await countAndListWorkflows(client, query);

    console.info("count ", result.count);

    const histories = result.executions.intoHistories();

    console.info("iterating to check  history.history field for each history");

    for await (const history of histories) {
        if (history.history === undefined) {
            console.warn("Got an undefined history object", history);
        }
    }


}


async function countAndListWorkflows(
    client: Client,
    query: string,
): Promise<{
    readonly count: number;
    readonly executions: AsyncWorkflowListIterable;
}> {
    const {count} =
        await client.workflow.workflowService.countWorkflowExecutions({
            namespace: client.options.namespace,
            query,
        });
    const executions = client.workflow.list({query});
    return {count: count.toNumber(), executions} as const;
}

run().catch((err) => {
    console.error(err);
    process.exit(1);
});

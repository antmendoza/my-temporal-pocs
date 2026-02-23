import { TestWorkflowEnvironment } from '@temporalio/testing';
import { Worker, Runtime, DefaultLogger, LogEntry } from '@temporalio/worker';
import { v4 as uuid4 } from 'uuid';
import { httpWorkflow, unblockSignal } from './workflows';

let testEnv: TestWorkflowEnvironment;


beforeAll(async () => {
  // Use console.log instead of console.error to avoid red output
  // Filter INFO log messages for clearer test output
  Runtime.install({
    logger: new DefaultLogger('WARN', (entry: LogEntry) => console.log(`[${entry.level}]`, entry.message)),
  });

  testEnv = await TestWorkflowEnvironment.createTimeSkipping();
});

afterAll(async () => {
  await testEnv?.teardown();
});

afterAll(() => {
});

async function setEnv() {
  const { client, nativeConnection } = testEnv;
  const worker = await Worker.create({
    connection: nativeConnection,
    taskQueue: 'test',
    workflowsPath: require.resolve('./workflows'),
    activities: {
      makeHTTPRequest: async () => 'we can ignore this result',
    },
  });
  return { client, worker };
}

test('timer fires, condition return false', async () => {
  const { client, worker } = await setEnv();

  await worker.runUntil(async () => {
    const result = await client.workflow.start(httpWorkflow, {
      workflowId: uuid4(),
      taskQueue: 'test',
    });
    // await testEnv.sleep('2m')
    expect(await result.result()).toEqual('false');
  });
});




test('condition become true', async () => {
  const { client, worker } = await setEnv();

  await worker.runUntil(async () => {
    const handler = await client.workflow.start(httpWorkflow, {
      workflowId: uuid4(),
      taskQueue: 'test',
    });



    await testEnv.sleep(1000) //this can be omitted

    await handler.signal(unblockSignal)
    expect(await handler.result()).toEqual('true');
  });
});

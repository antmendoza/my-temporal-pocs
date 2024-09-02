import { TestWorkflowEnvironment } from '@temporalio/testing';
import { Worker, Runtime, DefaultLogger, LogEntry } from '@temporalio/worker';
import { v4 as uuid4 } from 'uuid';
import { httpWorkflow } from './workflows';
import axios from "axios";
import * as activities from './activities';


let testEnv: TestWorkflowEnvironment;


beforeAll(async () => {
  // Use console.log instead of console.error to avoid red output
  // Filter INFO log messages for clearer test output
  Runtime.install({
    logger: new DefaultLogger('WARN', (entry: LogEntry) => console.log(`[${entry.level}]`, entry.message)),
  });

  testEnv = await TestWorkflowEnvironment.createLocal();
});

afterAll(async () => {
  await testEnv?.teardown();
});

afterAll(() => {

});

jest.mock('axios');
const mockedAxios = jest.mocked(axios, true);

test('httpWorkflow with mock activity', async () => {
    mockedAxios.get.mockResolvedValue({ data: { args: { answer: '99' } } });


  const { client, nativeConnection } = testEnv;
  const worker = await Worker.create({
      connection: nativeConnection,
      taskQueue: 'test',
      workflowsPath: require.resolve('./workflows'),
      activities: activities
      // {
      // makeHTTPRequest: async () => '99',
      // },
    }
  );

  await worker.runUntil(async () => {
    const result = await client.workflow.execute(httpWorkflow, {
      workflowId: uuid4(),
      taskQueue: 'test',
    });
    expect(result).toEqual('The answer is 99');
  });
});

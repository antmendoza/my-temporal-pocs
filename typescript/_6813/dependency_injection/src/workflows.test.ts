import {Runtime, DefaultLogger, Worker, LogEntry} from '@temporalio/worker';
import {TestWorkflowEnvironment} from '@temporalio/testing';
import {v4 as uuid} from 'uuid';
import {waitForConnectionCompletion} from "./workflows";
import {createActivities, Data, Endpoint} from "./activities";
import axios from "axios";


let testEnv: TestWorkflowEnvironment;

jest.mock('axios');
const mockedAxios = jest.mocked(axios, true);


describe('waitForConnectionCompletionWorkflow', () => {
    beforeAll(() => {
        // Use console.log instead of console.error to avoid red output
        // Filter INFO log messages for clearer test output
        Runtime.install({
            logger: new DefaultLogger('INFO', (entry: LogEntry) =>
                // eslint-disable-next-line no-console
                console.log(`[${entry.level}]`, entry.message)
            ),
        });
    });

    describe('if the connection does not complete within 10 minutes', () => {
        beforeAll(async () => {
            testEnv = await TestWorkflowEnvironment.createTimeSkipping();
        });

        afterAll(async () => {
            await testEnv?.teardown();
        });

        afterAll(() => {
            jest.clearAllMocks();
        });

        it('returns a completed status and connection_completed as false', async () => {

            const myMock: Endpoint = {
                get(key: string): Promise<string> {
                    return Promise.resolve("");
                }
            }


            const {client, nativeConnection} = testEnv as TestWorkflowEnvironment;
            const worker = await Worker.create(
                {
                    connection: nativeConnection,
                    taskQueue: 'test',
                    workflowsPath: require.resolve('./workflows'),
                    activities: createActivities(myMock),
                }
            );


            const result = await worker.runUntil(async () => {

                mockedAxios.get.mockResolvedValue({result: {data: {field1: "value1"}}});

                return await client.workflow.execute(waitForConnectionCompletion, {
                    taskQueue: 'test',
                    workflowId: `connecting_${uuid()}`,
                    args: ['id'],
                });
            });
            console.log(result)

            expect(result).toEqual(false);
        });
    });
});
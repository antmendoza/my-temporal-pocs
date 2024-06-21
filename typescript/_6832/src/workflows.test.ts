import { Runtime, DefaultLogger, Worker, LogEntry } from '@temporalio/worker';
import { WorkflowCoverage } from '@temporalio/nyc-test-coverage';
import { TestWorkflowEnvironment } from '@temporalio/testing';
import { v4 as uuid } from 'uuid';
import {waitForConnectionCompletion} from "./workflows";
import type * as activities from './activities';


const workflowCoverage = new WorkflowCoverage();
let testEnv: TestWorkflowEnvironment;

const mockActivities: Partial<typeof activities> = {
    sendSlackNotification: () => {
        return Promise.resolve({ success: true, metadata: {} });
    },
};

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
            workflowCoverage.mergeIntoGlobalCoverage();
        });

        it('returns a completed status and connection_completed as false', async () => {
            const { client, nativeConnection } = testEnv as TestWorkflowEnvironment;
            const worker = await Worker.create(
                workflowCoverage.augmentWorkerOptions({
                    connection: nativeConnection,
                    taskQueue: 'test',
                    workflowsPath: require.resolve('./workflows'),
                    activities: mockActivities,
                })
            );

            const result = await worker.runUntil(async () => {
                return await client.workflow.execute(waitForConnectionCompletion, {
                    taskQueue: 'test',
                    workflowId: `connecting_${uuid()}`,
                    args: ['id'],
                });
            });
            expect(result).toEqual(false);
        });
    });
});
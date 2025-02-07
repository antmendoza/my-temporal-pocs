import TestWorkflow1.TestWorkflow1;
import io.temporal.testing.internal.SDKTestOptions;
import io.temporal.testing.internal.SDKTestWorkflowRule;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.unsafe.WorkflowUnsafe;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
public class GetVersionRemovalTest {
    private static boolean hasReplayed;

    @Rule
    public SDKTestWorkflowRule testWorkflowRule =
            SDKTestWorkflowRule.newBuilder()
                    .setWorkflowTypes(TestGetVersionRemovalWorkflowImpl.class)
                    .setActivityImplementations(new TestActivitiesImpl())
                    // Forcing a replay. Full history arrived from a normal queue causing a replay.
                    .setWorkerOptions(
                            WorkerOptions.newBuilder()
                                    .setStickyQueueScheduleToStartTimeout(Duration.ZERO)
                                    .build())
                    .build();

    @Test
    public void testGetVersionMultithreadingRemoval() {
        TestWorkflow1 workflowStub =
                testWorkflowRule.newWorkflowStubTimeoutOptions(TestWorkflow1.class);
        String result = workflowStub.execute(testWorkflowRule.getTaskQueue());
        assertTrue(hasReplayed);
        assertEquals("activity:activity1", result);
    }

    public static class TestGetVersionRemovalWorkflowImpl implements TestWorkflow1 {
        @Override
        public String execute(String taskQueue) {
            VariousTestActivities testActivities =
                    Workflow.newActivityStub(
                            VariousTestActivities.class,
                            SDKTestOptions.newActivityOptionsForTaskQueue(taskQueue));

            // Test removing a version check in replaying code with an additional thread running.
            if (!WorkflowUnsafe.isReplaying()) {
                int version = Workflow.getVersion("changeId", 1, 2);
                assertEquals(version, 2);
            } else {
                hasReplayed = true;
                // This sideEffect following a getVersion causes NDE after the getVersion is removed
            }


            // This sideEffect following a getVersion causes NDE after the getVersion is removed
            Workflow.sideEffect(String.class, () -> System.getenv("USER"));

            String result =
                    "activity:" + testActivities.activity1(1); // This is executed in non-replay mode.
            return result;
        }
    }
}
package io.antmendoza.samples._20231205;

import io.antmendoza.samples.TestEnvironment;
import io.antmendoza.samples.TestUtilInterceptorTracker;
import io.antmendoza.samples._20231006.TestUtilWorkerInterceptor;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowUpdateException;
import io.temporal.failure.ApplicationFailure;
import io.temporal.testing.TestWorkflowRule;
import io.temporal.worker.WorkerFactoryOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class WizardUIWorkflowTest {


    private static TestUtilInterceptorTracker testUtilInterceptorTracker =
            new TestUtilInterceptorTracker();

    // set to true if you want to run the test against a real server
    private final boolean useExternalService = true;
    @Rule
    public TestWorkflowRule testWorkflowRule = createTestRule().build();

    @After
    public void after() {
        testWorkflowRule.getTestEnvironment().shutdown();
        testUtilInterceptorTracker = new TestUtilInterceptorTracker();
    }


    @Test
    public void testHappyPath() {
        final String namespace = testWorkflowRule.getTestEnvironment().getNamespace();
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mock(WizardUIActivity.WizardUIActivityImpl.class);

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);

        //start async
        final WorkflowExecution execution = WorkflowClient.start(workflowExecution::run, null);


        //get next screen
        assertEquals(
                ScreenID.SCREEN_1,
                workflowExecution.getCurrentScreen());

        String resultSubmitScreen_1 = workflowExecution.submitScreen(new UIData(Math.random() + ""));
        assertEquals(
                ScreenID.SCREEN_2.toString(),
                resultSubmitScreen_1);

        //verify activity invocations
        verify(activities, times(1)).activity1_1();
        verify(activities, times(1)).activity1_2();

        //get next screen
        assertEquals(
                ScreenID.SCREEN_2,
                workflowExecution.getCurrentScreen());

        String resultSubmitScreen_2 = workflowExecution.submitScreen(new UIData(Math.random() + ""));
        //verify activity invocations
        verify(activities, times(1)).activity2_1();
        assertEquals(
                ScreenID.SCREEN_3.toString(),
                resultSubmitScreen_2);

        //get next screen
        assertEquals(
                ScreenID.SCREEN_3,
                workflowExecution.getCurrentScreen());


        workflowExecution.forceMoveToScreen(ScreenID.SCREEN_1);
        assertEquals(
                ScreenID.SCREEN_1,
                workflowExecution.getCurrentScreen());


        String resultSubmitScreen_1_secondTime = workflowExecution.submitScreen(new UIData(Math.random() + ""));
        assertEquals(
                ScreenID.SCREEN_2.toString(),
                resultSubmitScreen_1_secondTime);

        //verify activity invocations
        verify(activities, times(2)).activity1_1();
        verify(activities, times(2)).activity1_2();

        workflowExecution.forceMoveToScreen(ScreenID.SCREEN_3);

        //get next screen
        assertEquals(
                ScreenID.SCREEN_3,
                workflowExecution.getCurrentScreen());

        //submit next screen
        String resultSubmitScreen_3 = workflowExecution.submitScreen(new UIData(Math.random() + ""));
        verify(activities, times(1)).activity3_1();
        verify(activities, times(1)).activity3_2();
        assertEquals(
                ScreenID.END.toString(),
                resultSubmitScreen_3);

        // wait for main workflow to complete
        workflowClient.newUntypedWorkflowStub(workflowId).getResult(Void.class);


        assertEquals(
                WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
                TestEnvironment.describeWorkflowExecution(execution, namespace, testWorkflowRule).getWorkflowExecutionInfo().getStatus());
    }

    @Test
    public void testConcurrentInvocationsToSubmitScreen() {
        final String namespace = testWorkflowRule.getTestEnvironment().getNamespace();
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mock(WizardUIActivity.WizardUIActivityImpl.class);
        //Add sleep during test to introduce delays on submitScreen method
        doCallRealMethod().when(activities).activity1_1();
        doCallRealMethod().when(activities).activity1_2();
        doCallRealMethod().when(activities).activity2_1();
        doCallRealMethod().when(activities).activity3_1();
        doCallRealMethod().when(activities).activity3_2();

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);


        //start async
        final WorkflowExecution execution = WorkflowClient.start(workflowExecution::run, null);


        CompletableFuture.runAsync(() -> {
            workflowExecution.submitScreen(new UIData(Math.random() + ""));
        });

        CompletableFuture.runAsync(() -> {
            workflowExecution.submitScreen(new UIData(Math.random() + ""));
        });

        CompletableFuture.runAsync(() -> {
            workflowExecution.submitScreen(new UIData(Math.random() + ""));
        });


        // wait for main workflow to complete
        workflowClient.newUntypedWorkflowStub(workflowId).getResult(Void.class);


        assertEquals(
                WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
                TestEnvironment.describeWorkflowExecution(execution, namespace, testWorkflowRule).getWorkflowExecutionInfo().getStatus());
    }


    @Test
    public void testValidateUpdate() {
        final String namespace = testWorkflowRule.getTestEnvironment().getNamespace();
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mock(WizardUIActivity.WizardUIActivityImpl.class);

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);


        //start async
        WorkflowClient.start(workflowExecution::run, null);


        try {
            workflowExecution.submitScreen(new UIData(null));
            Assert.fail();
        } catch (WorkflowUpdateException e) {
             assertEquals(
                    NullPointerException.class.getName(),
                     ((ApplicationFailure)e.getCause()).getType());

        }

    }


    private WizardUIWorkflow createWorkflowStub(String workflowId, WorkflowClient workflowClient) {
        final WorkflowOptions options =
                WorkflowOptions.newBuilder()
                        .setTaskQueue(testWorkflowRule.getTaskQueue())
                        .setWorkflowId(workflowId)
                        .build();
        final WizardUIWorkflow workflow =
                workflowClient.newWorkflowStub(WizardUIWorkflow.class, options);
        return workflow;
    }

    private TestWorkflowRule.Builder createTestRule() {
        TestWorkflowRule.Builder builder =
                TestWorkflowRule.newBuilder()
                        .setWorkerFactoryOptions(
                                WorkerFactoryOptions.newBuilder()
                                        .setWorkerInterceptors(
                                                new TestUtilWorkerInterceptor(testUtilInterceptorTracker))
                                        .build())
                        .setWorkflowTypes(
                                WizardUIWorkflow.WizardUIWorkflowImpl.class)
                        .setDoNotStart(true);

        if (useExternalService) {
            builder
                    .setUseExternalService(useExternalService)
                    .setTarget("127.0.0.1:7233") // default 127.0.0.1:7233
                    .setNamespace("default"); // default
        }

        return builder;
    }

}
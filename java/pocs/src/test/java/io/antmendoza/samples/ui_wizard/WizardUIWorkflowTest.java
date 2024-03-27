package io.antmendoza.samples.ui_wizard;

import com.google.common.collect.Lists;
import io.antmendoza.samples.TestEnvironment;
import io.antmendoza.samples.TestUtilInterceptorTracker;
import io.antmendoza.samples.TestUtilWorkerInterceptor;
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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class WizardUIWorkflowTest {


    private static TestUtilInterceptorTracker testUtilInterceptorTracker =
            new TestUtilInterceptorTracker();

    @Rule
    public TestWorkflowRule testWorkflowRule = createTestRule().build();
    private final String namespace = testWorkflowRule.getTestEnvironment().getNamespace();

    private static WizardUIActivity mockActivities() {
        WizardUIActivity activities = mock(WizardUIActivity.WizardUIActivityImpl.class);
        //Add sleep on purpose, to introduce delays on activity methods
        doCallRealMethod().when(activities).activity1_1();
        doCallRealMethod().when(activities).activity1_2();
        doCallRealMethod().when(activities).activity2_1();
        doCallRealMethod().when(activities).activity3_1();
        doCallRealMethod().when(activities).activity3_2();
        return activities;
    }

    @After
    public void after() {
        testWorkflowRule.getTestEnvironment().shutdown();
        testUtilInterceptorTracker = new TestUtilInterceptorTracker();
    }

    @Test
    public void testHappyPath() {
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mock(WizardUIActivity.WizardUIActivityImpl.class);

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);

        //start async
        final WorkflowExecution execution = WorkflowClient.start(workflowExecution::run);


        //get next screen
        assertEquals(
                ScreenID.SCREEN_1,
                workflowExecution.getCurrentScreen());

        String resultSubmitScreen_1 = workflowExecution.submitScreen(new UIRequest("1"));
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

        String resultSubmitScreen_2 = workflowExecution.submitScreen(new UIRequest("2"));
        //verify activity invocations
        verify(activities, times(1)).activity2_1();
        assertEquals(
                ScreenID.SCREEN_3.toString(),
                resultSubmitScreen_2);

        //get next screen
        assertEquals(
                ScreenID.SCREEN_3,
                workflowExecution.getCurrentScreen());


        String navigateToScreen_1 = workflowExecution.navigateTo(ScreenID.SCREEN_1);
        assertEquals(
                ScreenID.SCREEN_1.toString(),
                navigateToScreen_1);


        String resultSubmitScreen_1_secondTime = workflowExecution.submitScreen(new UIRequest("1"));
        assertEquals(
                ScreenID.SCREEN_2.toString(),
                resultSubmitScreen_1_secondTime);

        //verify activity invocations
        verify(activities, times(2)).activity1_1();
        verify(activities, times(2)).activity1_2();

        String navigateToScreen_3 = workflowExecution.navigateTo(ScreenID.SCREEN_3);
        assertEquals(
                ScreenID.SCREEN_3.toString(),
                navigateToScreen_3);

        //submit next screen
        String resultSubmitScreen_3 = workflowExecution.submitScreen(new UIRequest("3"));
        verify(activities, times(1)).activity3_1();
        verify(activities, times(1)).activity3_2();
        assertEquals(
                ScreenID.END.toString(),
                resultSubmitScreen_3);

        // wait for workflow to complete
        workflowClient.newUntypedWorkflowStub(workflowId).getResult(Void.class);


        assertEquals(
                WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
                TestEnvironment.describeWorkflowExecution(execution, namespace, testWorkflowRule).getWorkflowExecutionInfo().getStatus());
    }

    @Test
    public void testConcurrentInvocationsToSubmitScreen() throws InterruptedException {
        final String namespace = testWorkflowRule.getTestEnvironment().getNamespace();
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mockActivities();

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);


        //start async
        final WorkflowExecution execution = WorkflowClient.start(workflowExecution::run);

        final List<String> results = Lists.newArrayList(workflowExecution.getCurrentScreen().toString());


        //Concurrent invocations to updateWorkflow
        //TODO use ExecutorService
        IntStream.rangeClosed(1, 2).parallel().forEach(r -> {
            CompletableFuture.runAsync(() -> results.add(workflowExecution
                    .submitScreen(new UIRequest(r + ""))));

            CompletableFuture.runAsync(() -> results.add(workflowExecution
                    .submitScreen(new UIRequest(r + "")))
            );
        });

        //Wait to ensure the request for the screen 3 is submitted the latest one, to close the workflow
        Thread.sleep(1000); //TODO
        results.add(workflowExecution
                .submitScreen(new UIRequest("3")));


        // wait for workflow to complete
        workflowClient.newUntypedWorkflowStub(workflowId).getResult(Void.class);


        assertEquals(
                WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
                TestEnvironment.describeWorkflowExecution(execution, namespace, testWorkflowRule).getWorkflowExecutionInfo().getStatus());

        verify(activities, times(2)).activity1_1();
        verify(activities, times(2)).activity1_2();
        verify(activities, times(2)).activity2_1();
        verify(activities, times(1)).activity3_1();
        verify(activities, times(1)).activity3_2();


        assertThat(results, containsInAnyOrder(
                ScreenID.SCREEN_1.toString(),
                ScreenID.SCREEN_2.toString(),
                ScreenID.SCREEN_2.toString(),
                ScreenID.SCREEN_3.toString(),
                ScreenID.SCREEN_3.toString(),
                ScreenID.END.toString()
        ));


    }

    @Test
    public void testValidateUpdate() {
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mock(WizardUIActivity.WizardUIActivityImpl.class);

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);


        //start async
        WorkflowClient.start(workflowExecution::run);


        try {
            workflowExecution.submitScreen(new UIRequest(null));
            Assert.fail();
        } catch (WorkflowUpdateException e) {
            assertEquals(
                    NullPointerException.class.getName(),
                    ((ApplicationFailure) e.getCause()).getType());
        }
    }


    @Test
    public void testSendNotification() {
        final String workflowId = "my-workflow-" + Math.random();

        WizardUIActivity activities = mockActivities();

        testWorkflowRule.getWorker().registerActivitiesImplementations(activities);
        testWorkflowRule.getTestEnvironment().start();

        final WorkflowClient workflowClient = testWorkflowRule.getWorkflowClient();
        final WizardUIWorkflow workflowExecution =
                createWorkflowStub(workflowId, workflowClient);


        //start async
        final WorkflowExecution execution = WorkflowClient.start(workflowExecution::run);

        verify(activities, times(0)).sendNotification();

        testWorkflowRule.getTestEnvironment().sleep(Duration.ofSeconds(4));
        verify(activities, times(1)).sendNotification();

        workflowExecution.submitScreen(new UIRequest("1"));
        testWorkflowRule.getTestEnvironment().sleep(Duration.ofSeconds(2));

        workflowExecution.submitScreen(new UIRequest("1"));
        testWorkflowRule.getTestEnvironment().sleep(Duration.ofSeconds(2));


        //sendNotification should not be executed again
        verify(activities, times(1)).sendNotification();

        workflowExecution.submitScreen(new UIRequest("3"));

        // wait for workflow to complete
        workflowClient.newUntypedWorkflowStub(workflowId).getResult(Void.class);

        assertEquals(
                WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED,
                TestEnvironment.describeWorkflowExecution(execution, namespace, testWorkflowRule).getWorkflowExecutionInfo().getStatus());

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
                                WizardUIWorkflow.WizardUIWorkflowImplBufferRequests.class)
                        .setDoNotStart(true);


        // set to true if you want to run the test with a "real" server
        final boolean useExternalService = Boolean.parseBoolean(System.getenv("TEST_LOCALHOST"));

        if (useExternalService) {
            builder
                    .setUseExternalService(useExternalService)
                    .setTarget("127.0.0.1:7233") // default 127.0.0.1:7233
                    .setNamespace("default"); // default
        }

        return builder;
    }

}
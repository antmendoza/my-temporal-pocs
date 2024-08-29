

package com.antmendoza;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.api.enums.v1.ScheduleOverlapPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;
import java.util.Collections;

public class HelloSchedules {

    static final String TASK_QUEUE = "HelloScheduleTaskQueue";

    static final String WORKFLOW_ID = "HelloScheduleWorkflow";

    static final String SCHEDULE_ID = "HelloSchedule";

    public static void main(String[] args) throws InterruptedException {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

        factory.start();

        ScheduleClient scheduleClient = ScheduleClient.newInstance(service);

        WorkflowOptions workflowOptions =
                WorkflowOptions.newBuilder().setWorkflowId(WORKFLOW_ID).setTaskQueue(TASK_QUEUE).build();

        try {
            scheduleClient.getHandle(SCHEDULE_ID).delete();
        } catch (Exception e) {
            System.out.println("---" + e);
        }

        ScheduleActionStartWorkflow action =
                ScheduleActionStartWorkflow.newBuilder()
                        .setWorkflowType(HelloSchedules.GreetingWorkflow.class)
                        .setArguments("World")
                        .setOptions(workflowOptions)
                        .build();

        final ScheduleOverlapPolicy overlapPolicy = ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP;

        Schedule schedule =
                Schedule.newBuilder()
                        .setAction(action)
                        .setPolicy(SchedulePolicy.newBuilder().setOverlap(overlapPolicy).build())
                        .setSpec(ScheduleSpec.newBuilder().build())
                        .build();

        // Create a schedule on the server
        ScheduleHandle handle =
                scheduleClient.createSchedule(SCHEDULE_ID, schedule, ScheduleOptions.newBuilder().build());

        // Update the schedule with a spec, so it will run periodically
        handle.update(
                (ScheduleUpdateInput input) -> {
                    Schedule.Builder builder = Schedule.newBuilder(input.getDescription().getSchedule());

                    builder.setPolicy(SchedulePolicy.newBuilder().setOverlap(overlapPolicy).build());

                    builder.setSpec(
                            ScheduleSpec.newBuilder()
                                    // Run the schedule every 3s
                                    .setIntervals(
                                            Collections.singletonList(new ScheduleIntervalSpec(Duration.ofSeconds(3))))
                                    .build());
                    return new ScheduleUpdate(builder.build());
                });

        // The workflow prints getLastCompletionResult

        Thread.sleep(10_000);

        pauseSchedule(handle);

        waitRunsToComplete(client);

        final ScheduleOverlapPolicy overlapPolicyAllowAll =
                ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_ALLOW_ALL;

        System.out.println("---Updating schedule overlapPolicy = " + overlapPolicyAllowAll);
        updateSchedule(scheduleClient, overlapPolicyAllowAll);

        System.out.println("---UnPause schedule");
        handle.unpause();

        Thread.sleep(10_000);

        pauseSchedule(handle);

        waitRunsToComplete(client);

        // Delete the schedule once the sample is done
        handle.delete();
        System.exit(0);
    }

    private static void waitRunsToComplete(WorkflowClient client) throws InterruptedException {
        System.out.println("---Waiting for remaining runs to complete ");

        while (client
                .listExecutions(
                        "`TemporalScheduledById`=\"" + SCHEDULE_ID + "\" AND `ExecutionStatus`=\"Running\"")
                .count()
                > 0) {

            Thread.sleep(1_000);
        }
    }

    private static void pauseSchedule(final ScheduleHandle handle) {
        System.out.println("---Pause schedule");
        handle.pause();
    }

    private static void updateSchedule(
            ScheduleClient scheduleClient, final ScheduleOverlapPolicy scheduleOverlapPolicyAllowAll) {

        // Create a schedule on the server
        ScheduleHandle handle = scheduleClient.getHandle(SCHEDULE_ID);

        handle.update(
                (ScheduleUpdateInput input) -> {
                    Schedule.Builder builder = Schedule.newBuilder(input.getDescription().getSchedule());

                    builder.setPolicy(
                            SchedulePolicy.newBuilder().setOverlap(scheduleOverlapPolicyAllowAll).build());

                    return new ScheduleUpdate(builder.build());
                });
    }

    @WorkflowInterface
    public interface GreetingWorkflow {

        @WorkflowMethod
        String greet(String name);
    }

    @ActivityInterface
    public interface GreetingActivities {

        void greet(String greeting);
    }

    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        @Override
        public String greet(String name) {

            System.out.println(
                    "Workflow.getLastCompletionResult: " + Workflow.getLastCompletionResult(String.class));

            Workflow.sleep(Duration.ofSeconds(5));

            final String workflowId = Workflow.getInfo().getWorkflowId();
            System.out.println("Workflow result : " + workflowId);
            return workflowId;
        }
    }

    static class GreetingActivitiesImpl implements GreetingActivities {
        @Override
        public void greet(String greeting) {
            System.out.println(
                    "From " + Activity.getExecutionContext().getInfo().getWorkflowId() + ": " + greeting);
        }
    }
}

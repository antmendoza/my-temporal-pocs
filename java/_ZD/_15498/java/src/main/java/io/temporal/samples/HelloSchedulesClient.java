package io.temporal.samples;

import static io.temporal.samples.HelloWORKER.TASK_QUEUE;
import static io.temporal.samples.HelloWORKER.WORKFLOW_ID;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import io.temporal.config.Client;
import io.temporal.samples.proto.Fullname;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.List;

public class HelloSchedulesClient {

  private static final String SCHEDULE_ID = "HelloSchedule";

  public static void main(String[] args) throws InterruptedException {

    Client client = new Client();
    WorkflowServiceStubs workflowServiceStubs = client.getWorkflowServiceStubs();
    ScheduleClient scheduleClient =
        ScheduleClient.newInstance(
            workflowServiceStubs,
            ScheduleClientOptions.newBuilder()
                .setNamespace(client.sslContextBuilderProvider.properties.getTemporalNamespace())
                .build());

    try {
      scheduleClient.getHandle(SCHEDULE_ID).delete();

    } catch (Exception e) {
      // ignore
    }

    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder().setWorkflowId(WORKFLOW_ID).setTaskQueue(TASK_QUEUE).build();

    // Build a custom protobuf message with two fields (name, surname)
    Fullname inputProto = Fullname.newBuilder().setName("Jane").setSurname("Doe").build();

    ScheduleActionStartWorkflow action =
        ScheduleActionStartWorkflow.newBuilder()
            .setWorkflowType(HelloWORKER.GreetingWorkflow.class)
            .setArguments(inputProto)
            .setOptions(workflowOptions)
            .build();

    // Define the schedule we want to create
    Schedule schedule =
        Schedule.newBuilder().setAction(action).setSpec(ScheduleSpec.newBuilder()
                        .setCronExpressions(List.of("*/1 * * * *"))
                .build()).build();

    // Create a schedule on the server
    scheduleClient.createSchedule(SCHEDULE_ID, schedule, ScheduleOptions.newBuilder().build());

   System.exit(0);
  }
}

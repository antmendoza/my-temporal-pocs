package com.antmendoza.generator;

import io.temporal.api.history.v1.History;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.common.WorkflowExecutionHistory;
import io.temporal.serviceclient.WorkflowServiceStubs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PrepareTestFiles {
  private static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
  private static final WorkflowClient client = WorkflowClient.newInstance(service);

  public static void main(String[] args) {

    final Path path = Path.of("src/test/resources", "");

    generateAndSaveHistoriesToFolder(path);

    System.exit(0);
  }

  private static void generateAndSaveHistoriesToFolder(final Path path) {
    MyWorker.runWorker();
    for (int i = 0; i < 10; i++) {
      MyStarter.start();
    }

    // wait to ensure workflow executions complete
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    client
        .listExecutions("")
        .forEach(
            f -> {
              final GetWorkflowExecutionHistoryRequest getWorkflowExecutionHistoryRequest =
                  GetWorkflowExecutionHistoryRequest.newBuilder()
                      .setExecution(f.getExecution())
                      .setNamespace("default")
                      .build();
              final History history =
                  client
                      .getWorkflowServiceStubs()
                      .blockingStub()
                      .getWorkflowExecutionHistory(getWorkflowExecutionHistoryRequest)
                      .getHistory();

              String json = new WorkflowExecutionHistory(history).toJson(true);

              try {
                Files.writeString(
                    Path.of(
                        path.toString(),
                        f.getWorkflowExecutionInfo().getExecution().getRunId() + ".json"),
                    json);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }
}

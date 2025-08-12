package io.temporal.samples.tracing;

import static io.temporal.samples.tracing.Mode.mode;

import io.temporal.client.WorkflowClient;
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.samples.tracing.workflow.TracingActivitiesImpl;
import io.temporal.samples.tracing.workflow.TracingWorkflowImpl;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;

public class TracingWorker {
  private static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
  private static final WorkflowClient client = WorkflowClient.newInstance(service);
  public static final String TASK_QUEUE_NAME = "tracingTaskQueue";

  public static void main(String[] args) {
    String type = mode;

    // Set the OpenTracing client interceptor
    WorkerFactoryOptions factoryOptions =
        WorkerFactoryOptions.newBuilder()
            .setWorkerInterceptors(
                new OpenTracingWorkerInterceptor(JaegerUtils.getJaegerOptions(type)))
            .build();
    WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);

    Worker worker = factory.newWorker(TASK_QUEUE_NAME);
    worker.registerWorkflowImplementationTypes(TracingWorkflowImpl.class);
    worker.registerActivitiesImplementations(new TracingActivitiesImpl());

    factory.start();
  }
}

package io.temporal.samples.hello;

import io.grpc.*;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Starter_API_KEY {

  private static final String TASK_QUEUE = "hello-world-task-queue";

  public static void main(String[] args) throws InterruptedException {

    final String namespace = "antonio.a2dd6";

    // load YOUR_API_KEY from argument
    //
    final String key = System.getenv("TEMPORAL_API_KEY");
    final ClientInterceptor interceptr =
        new ClientInterceptor() {
          @Override
          public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
              MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

            // Print api key value
            return next.newCall(method, callOptions);
          }
        };
    final Collection<ClientInterceptor> interceptor =
        List.of(interceptr, new HeaderLoggingInterceptor());

    WorkflowServiceStubsOptions.Builder stubOptions =
        WorkflowServiceStubsOptions.newBuilder()
            .addApiKey(() -> key)
            .setEnableHttps(true)
            .addGrpcMetadataProvider(
                () -> {
                  Metadata metadata = new Metadata();
                  metadata.put(
                      Metadata.Key.of("temporal-namespace", Metadata.ASCII_STRING_MARSHALLER),
                      namespace);
                  return metadata;
                })
            .setGrpcClientInterceptors(interceptor)
            .setTarget("us-west-2.aws.api.temporal.io:7233");

    WorkflowServiceStubs service =
        WorkflowServiceStubs.newConnectedServiceStubs(stubOptions.build(), Duration.ofSeconds(10));

    WorkflowClientOptions clientOptions =
        WorkflowClientOptions.newBuilder()
            .setNamespace(namespace)
            //                        .setDataConverter(dataConverter)
            .build();
    WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);

    final int millisSleep = 1000;

    final AtomicInteger input = new AtomicInteger();
    while (input.get() < 2) {

      final int andIncrement = input.getAndIncrement();
      CompletableFuture.runAsync(
          () -> {
            final String workflowId = andIncrement + "-" + Math.random();
            try {

              WorkflowOptions workflowOptions =
                  WorkflowOptions.newBuilder()
                      .setTaskQueue(TASK_QUEUE)
                      .setWorkflowId(workflowId)
                      .build();

              MyWorkflow1 workflow = client.newWorkflowStub(MyWorkflow1.class, workflowOptions);
              System.out.println("Starting workflow...with after = " + millisSleep + " ms");
              System.out.println(workflowId + "init " + new Date());
              WorkflowExecution execution = WorkflowClient.start(workflow::run, "" + andIncrement);
              System.out.println(workflowId + "end " + new Date());

            } catch (Exception e) {

              System.out.println("Failed workflowId = " + workflowId);
            }
          });

      Thread.sleep(millisSleep);
    }
  }

  public static class HeaderLoggingInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

      return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
          next.newCall(method, callOptions)) {

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {

          // Log request headers
          System.out.println(
              new Date() + " Request Headers: " + " " + method.getFullMethodName() + " " + headers);
          super.start(
              new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                  responseListener) {
                @Override
                public void onHeaders(Metadata headers) {
                  // Log response headers
                  System.out.println(
                      new Date()
                          + " Response Headers: "
                          + " "
                          + method.getFullMethodName()
                          + " "
                          + headers);
                  super.onHeaders(headers);
                }
              },
              headers);
        }
      };
    }
  }

  @WorkflowInterface
  public interface MyWorkflow1 {
    @WorkflowMethod
    String run(String s);
  }
}

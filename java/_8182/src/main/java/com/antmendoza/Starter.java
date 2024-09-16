package com.antmendoza;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.util.AdvancedTlsX509KeyManager;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Starter {

  static final String TASK_QUEUE = "MyTaskQueue";
  static final String WORKFLOW_ID = "HelloSSLWorkflow";

  public static void main(String[] args) throws Exception {
    // Load your client certificate, which should look like:
    // -----BEGIN CERTIFICATE-----
    // ...
    // -----END CERTIFICATE-----
    File clientCertFile = new File(System.getenv("TEMPORAL_CLIENT_CERT"));
    // PKCS8 client key, which should look like:
    // -----BEGIN PRIVATE KEY-----
    // ...
    // -----END PRIVATE KEY-----
    File clientKeyFile = new File(System.getenv("TEMPORAL_CLIENT_KEY"));
    // For temporal cloud this would likely be ${namespace}.tmprl.cloud:7233
    String targetEndpoint = System.getenv("TEMPORAL_ENDPOINT");
    // Your registered namespace.
    String namespace = System.getenv("TEMPORAL_NAMESPACE");
    // How often to refresh the client key and certificate
    String refreshPeriodString = System.getenv("TEMPORAL_CREDENTIAL_REFRESH_PERIOD");
    long refreshPeriod = refreshPeriodString != null ? Integer.parseInt(refreshPeriodString) : 0;
    // Create SSL context from SimpleSslContextBuilder
    SslContext sslContext =
        SimpleSslContextBuilder.forPKCS8(
                new FileInputStream(clientCertFile), new FileInputStream(clientKeyFile))
            .build();
    // To refresh the client key and certificate, create an AdvancedTlsX509KeyManager and manually
    // configure the SSL context.
    if (refreshPeriod > 0) {
      AdvancedTlsX509KeyManager clientKeyManager = new AdvancedTlsX509KeyManager();
      // Reload credentials every minute
      clientKeyManager.updateIdentityCredentialsFromFile(
          clientKeyFile,
          clientCertFile,
          refreshPeriod,
          TimeUnit.MINUTES,
          Executors.newScheduledThreadPool(1));
      sslContext =
          GrpcSslContexts.configure(SslContextBuilder.forClient().keyManager(clientKeyManager))
              .build();
    }

    // Create SSL enabled client by passing SslContext
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setSslContext(sslContext)
                .setTarget(targetEndpoint)
                .build());

    // Now setup and start workflow worker, which uses SSL enabled gRPC service to communicate with
    // backend.
    // client that can be used to start and signal workflows.
    WorkflowClient client =
        WorkflowClient.newInstance(
            service, WorkflowClientOptions.newBuilder().setNamespace(namespace).build());
    // worker factory that can be used to create workers for specific task queues
    WorkerFactory factory = WorkerFactory.newInstance(client);

    /*
     * Define the workflow worker. Workflow workers listen to a defined task queue and process
     * workflows and activities.
     */
    Worker worker = factory.newWorker(TASK_QUEUE);

    /*
     * Register our workflow implementation with the worker.
     * Workflow implementations must be known to the worker at runtime in
     * order to dispatch workflow tasks.
     */
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);

    /*
     * Register our Activity Types with the Worker. Since Activities are stateless and thread-safe,
     * the Activity Type is a shared instance.
     */
    // worker.registerActivitiesImplementations(...);

    /*
     * Start all the workers registered for a specific task queue.
     * The started workers then start polling for workflows and activities.
     */
    // factory.start();

    // create a pool of threads, 10 max jobs will execute in parallel
    ExecutorService threadPool = Executors.newFixedThreadPool(1000);
    // submit jobs to be executing by the pool
    for (int i = 0; i < 10; i++) {
      threadPool.submit(
          new Runnable() {
            public void run() {

              final String workflowId = WORKFLOW_ID + Math.random();

              MyWorkflow workflow =
                  client.newWorkflowStub(
                      MyWorkflow.class,
                      WorkflowOptions.newBuilder()
                          .setWorkflowId(workflowId)
                          .setTaskQueue(TASK_QUEUE)
                          .build());
              WorkflowClient.start(workflow::execute);

              threadPool.submit(
                  new Runnable() {
                    public void run() {

                      for (int b = 0; b < 50; b++) {
                        final int finalB = b;
                        threadPool.submit(
                            new Runnable() {
                              public void run() {

                                System.out.println(
                                    "query1_ " + workflowId + " - " + new Date().getTime());
                                client
                                    .newUntypedWorkflowStub(workflowId)
                                    .query("myquery", Starter.class);

                                if (finalB % 5 == 0) {
                                  try {
                                    Thread.sleep(100);
                                  } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                  }
                                }
                              }
                            });
                      }
                    }
                  });
            }
          });

      if (i % 5 == 0) {
        Thread.sleep(500);
      }
    }
    // once you've submitted your last job to the service it should be shut down
    // threadPool.shutdown();
  }
}

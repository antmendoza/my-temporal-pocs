package com.example.service2.worker;

import com.example.service2.TraceUtils;
import com.example.service2.activities.TestActivityImpl;
import com.example.service2.workflows.TestWorkflowImpl;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkflowImplementationOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class WorkerStarter {


    private static boolean hasWorker(final WorkerFactory factory, final String taskQueue) {
        try {
            return factory.getWorker(taskQueue) == null;
        } catch (Exception e) {
            return false;
        }
    }

    public void run() {
        WorkerFactory factory = createWorkerFactory();

        final String taskQueue = "TASK_QUEUE";
        if (!hasWorker(factory, taskQueue)) {
            Worker worker = factory.newWorker(taskQueue);
            WorkflowImplementationOptions workflowOptions = createWorkflowImplementationOptions();
            worker.registerWorkflowImplementationTypes(workflowOptions, TestWorkflowImpl.class);
            worker.registerActivitiesImplementations(new TestActivityImpl());
            factory.start();
        }

    }

    private WorkerFactory createWorkerFactory() {


        WorkflowClient client = createWorkflowClient();

        WorkerFactoryOptions factoryOptions =
                WorkerFactoryOptions.newBuilder()
                        .setWorkerInterceptors(
                                new OpenTracingWorkerInterceptor(
                                        TraceUtils.getOptions()
                                ))
                        .build();


        WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);


        return factory;
    }

    private WorkflowClient createWorkflowClient() {
        WorkflowServiceStubs serviceStubs = createWorkflowServiceStubs();
        return WorkflowClient.newInstance(serviceStubs);
    }

    private WorkflowServiceStubs createWorkflowServiceStubs() {
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    private WorkflowImplementationOptions createWorkflowImplementationOptions() {
        return WorkflowImplementationOptions.newBuilder()
                .setActivityOptions(createActivityOptionsMap())
                .build();
    }

    private Map<String, ActivityOptions> createActivityOptionsMap() {
        Map<String, ActivityOptions> activityOptionsMap = new HashMap<>();
        activityOptionsMap.put("PrintHello", createActivityOptions());
        return activityOptionsMap;
    }

    private ActivityOptions createActivityOptions() {
        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(60))
                .build();
    }
}

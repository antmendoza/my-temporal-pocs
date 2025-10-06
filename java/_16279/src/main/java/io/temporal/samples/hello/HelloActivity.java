package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.Payload;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.context.ContextPropagator;
import io.temporal.common.converter.DataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class HelloActivity {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";

    @WorkflowInterface
    public interface GreetingWorkflow {

        @WorkflowMethod
        String run();
    }

    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod(name = "greet")
        String extractContext();
    }

    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String run() {
            return activities.extractContext();
        }
    }

    /** Simple activity implementation, that concatenates two strings. */
    public static class GreetingActivitiesImpl implements GreetingActivities {

        @Override
        public String extractContext() {
            return MDC.get("my-context");
        }
    }

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        WorkflowClient client =
                WorkflowClient.newInstance(
                        service,
                        WorkflowClientOptions.newBuilder()
                                .setContextPropagators(Collections.singletonList(new MDCContextPropagator()))
                                .build());

        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

        factory.start();

        MDC.put("my-context", "hello from context propagator");

        // Create the workflow client stub. It is used to start our workflow execution.
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(TASK_QUEUE)
                                .build());

        String greeting = workflow.run();

        System.out.println(greeting);
        System.exit(0);
    }

    public static class MDCContextPropagator implements ContextPropagator {


        @Override
        public String getName() {
            return this.getClass().getName();
        }

        @Override
        public Object getCurrentContext() {
            Map<String, String> context = new HashMap<>();
            if (MDC.getCopyOfContextMap() == null) {
                return context;
            }
            for (Map.Entry<String, String> entry : MDC.getCopyOfContextMap().entrySet()) {
                if (entry.getKey().startsWith("my-context")) {
                    context.put(entry.getKey(), entry.getValue());
                }
            }
            return context;
        }

        @Override
        public void setCurrentContext(Object context) {
            Map<String, String> contextMap = (Map<String, String>) context;
            for (Map.Entry<String, String> entry : contextMap.entrySet()) {
                MDC.put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Map<String, Payload> serializeContext(Object context) {
            Map<String, String> contextMap = (Map<String, String>) context;
            Map<String, Payload> serializedContext = new HashMap<>();
            for (Map.Entry<String, String> entry : contextMap.entrySet()) {
                serializedContext.put(
                        entry.getKey(), DataConverter.getDefaultInstance().toPayload(entry.getValue()).get());
            }
            return serializedContext;
        }

        @Override
        public Object deserializeContext(Map<String, Payload> context) {
            Map<String, String> contextMap = new HashMap<>();
            for (Map.Entry<String, Payload> entry : context.entrySet()) {
                contextMap.put(
                        entry.getKey(),
                        DataConverter.getDefaultInstance()
                                .fromPayload(entry.getValue(), String.class, String.class));
            }
            return contextMap;
        }
    }
}



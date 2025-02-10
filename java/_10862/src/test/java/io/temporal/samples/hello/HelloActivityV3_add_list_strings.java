package io.temporal.samples.hello;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;


public class HelloActivityV3_add_list_strings {


    @WorkflowInterface
    public interface GreetingWorkflow {


        @WorkflowMethod
        String getGreeting(String name, List<String> names);


        @SignalMethod
        void signal(String name, List<String> names);


    }


    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private boolean signaled = false;

        @Override
        public String getGreeting(String name, List<String> names) {

            // This is a blocking call that returns only after the activity has completed.
            Workflow.await(() -> {
                return this.signaled;
            });

            return "done";
        }

        @Override
        public void signal(final String name, List<String> names) {
            System.out.println("Signal received");
            this.signaled = true;
        }
    }


}

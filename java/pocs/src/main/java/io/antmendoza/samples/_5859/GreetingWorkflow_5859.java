package io.antmendoza.samples._5859;

import com.google.common.collect.ImmutableSet;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.unsafe.WorkflowUnsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@WorkflowInterface
public interface GreetingWorkflow_5859 {

    @WorkflowMethod
    String getGreeting();

    @QueryMethod
    String get__();



    @ActivityInterface
    public interface GreetingActivities {

        @ActivityMethod
        ImmutableSet<MyActivityResponse> execute();


    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    static class GreetingActivitiesImpl implements GreetingActivities {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

        @Override
        public ImmutableSet<MyActivityResponse> execute() {
            MyActivityResponse a = new MyActivityResponse("A","B");
            return ImmutableSet.of(a);
//            return new MyActivityResponse("A","B");
        }
    }


    public class GreetingWorkflowImpl implements GreetingWorkflow_5859 {

        private final GreetingActivities activities =
                Workflow.newLocalActivityStub(
                        GreetingActivities.class,
                        LocalActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting() {
            Object response = activities.execute();

            if(WorkflowUnsafe.isReplaying()){
                System.out.println("During replay ");
                System.out.println(">>>" + response);
            }else {
                System.out.println("First run  ");
                System.out.println(">>>" + response);
            }

            Workflow.sleep(Duration.ofSeconds(10));

            return "done";
        }

        @Override
        public String get__() {
            return null;
        }


    }

}


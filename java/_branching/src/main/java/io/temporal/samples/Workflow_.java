package io.temporal.samples;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.TemporalFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Workflow_ {

    @WorkflowInterface
    public interface ParentWorkflow {

        @WorkflowMethod
        List<BranchOutput> start(final WorkflowInput workflowInput);


        @SignalMethod
        void processSignal(String jobId);
    }

    @ActivityInterface
    public interface MyActivities {

        @ActivityMethod
        String startJob(String jobId);
    }

    public static class ParentWorkflowImpl implements ParentWorkflow {

        private final MyActivities activities =
                Workflow.newActivityStub(
                        MyActivities.class,
                        ActivityOptions.newBuilder()
                                .setStartToCloseTimeout(Duration.ofSeconds(5)).build());

        private final Logger logger = Workflow.getLogger(ParentWorkflowImpl.class);


        private final List<String> jobs = new ArrayList<>();

        @Override
        public List<BranchOutput> start(final WorkflowInput workflowInput) {

            List<Promise<BranchOutput>> promises = new ArrayList<>();

            // Simulate some work
            promises.add(Async.function(this::startJob, "job1")
                    .thenApply(result -> new BranchOutput("job1", result)));
            promises.add(Async.function(this::startJob, "job2")
                    .thenApply(result -> new BranchOutput("job2", result)));
            promises.add(Async.function(this::startJob, "job3")
                    .thenApply(result -> new BranchOutput("job3", result)));

            try {
                //wait for all branches to complete
                Promise.allOf(promises).get();
            } catch (TemporalFailure e) {

                //handle failure if one of the branches fails

                //                for (Promise<String> promise : promises) {
                //                    if (promise.getFailure() != null) {
                //                        promise.get();
                //                    }
                //                }
            }

            return promises.stream().map(Promise::get).toList();
        }

        @Override
        public void processSignal(String jobId) {
            this.jobs.add(jobId);
        }

        private boolean startJob(String jobId) {


           activities.startJob(jobId);

            //wait for signal to be received with the same jobId
            boolean signalReceived = Workflow.await(Duration.ofSeconds(2), () -> jobs.contains(jobId));


            //timeout
            if (!signalReceived) {
                //do whatever the business logic needs to do,retry activity?
                // for the sake of this example, we will just retry the activity
                activities.startJob(jobId);
                // and return false
                return false;

            }


            return true;

        }


    }

    static class MyActivitiesImpl implements MyActivities {


        @Override
        public String startJob(String jobId) {

            return null;
        }

    }


    static class BranchOutput {

        public String jobId;
        public boolean result;

        public BranchOutput() {

        }


        public BranchOutput(String jobId, boolean result) {
            this.jobId = jobId;
            this.result = result;


        }

        public String getJobId() {
            return jobId;
        }

        public boolean isResult() {
            return result;
        }


        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public void setResult(boolean result) {
            this.result = result;
        }
    }

}

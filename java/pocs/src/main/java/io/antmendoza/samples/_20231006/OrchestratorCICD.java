package io.antmendoza.samples._20231006;

import io.antmendoza.samples._20231006.StageB.StageBRequest;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.failure.CanceledFailure;
import io.temporal.failure.ChildWorkflowFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;

@WorkflowInterface
public interface OrchestratorCICD {

    @WorkflowMethod
    void run(OrchestratorRequest request);

    // We use the main workflow as interface to communicate with their child workflows.
    // there is nothing wrong signaling childworkflows directly
    @SignalMethod
    void manualVerificationStageA(StageA.VerificationStageARequest request);

    @SignalMethod
    void manualVerificationStageB(StageB.VerificationStageBStatus verificationStageBStatus);

    class OrchestratorCICDImpl implements OrchestratorCICD {

        private final Logger log = Workflow.getLogger("OrchestratorCICDImpl");

        private StageA stageA;
        private StageB stageB;

        private static String getWorkflowId() {
            return Workflow.getInfo().getWorkflowId();
        }

        private static boolean executeSteps(StageResult stageResult) {
            return stageResult == null || stageResult.getVerificationStageBStatus().retryFromStageA();
        }

        @Override
        public void run(OrchestratorRequest request) {

            String workflowId = getWorkflowId();
            log.info("Starting with runId:" + Workflow.getInfo().getRunId());

            StageResult stageResult = new StageResult("stageA");

            while (calculateNextStep(stageResult) != null) {
                // TODO watch workflow history and continueAsNew when required
                switch (calculateNextStep(stageResult)){

                    case "stageA":
                        stageResult = executeStageA(workflowId);
                        break;

                    case "stageB":
                        stageResult = executeStageB(workflowId);
                        break;

                    default:
                        break;

                }

            }
        }

        private String calculateNextStep(final StageResult stageResult) {


            if (stageResult.getVerificationStageBStatus() != null &&
                    stageResult.getVerificationStageBStatus().retryFromStageA()){
                return  "stageA";
            }

            return stageResult.getStage();



        }

        private StageResult executeStageB(String workflowId) {
            StageResult stageResult;

            //or start the child workflow sync
            stageB = Workflow.newChildWorkflowStub(StageB.class,
                    ChildWorkflowOptions.newBuilder()
                            .setWorkflowId(StageB.buildWorkflowId(workflowId)).build());
            final Promise<StageResult> resultStageB = Async.function(stageB::run, new StageBRequest());
            final Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(stageB);

            // Wait for child to start
            childExecution.get();

            // Wait for the stageB to complete
            Promise.allOf(resultStageB).get();

            // TODO handle
            stageResult = resultStageB.get();
            return stageResult;
        }

        private StageResult executeStageA(String workflowId) {
            stageA = Workflow.newChildWorkflowStub(StageA.class,
                    ChildWorkflowOptions.newBuilder()
                            .setWorkflowId(StageA.buildWorkflowId(workflowId))
                            .build());

            //or start the child workflow sync
            final Promise<StageResult> resultStageA = Async.function(stageA::run, new StageA.StageARequest());
            final Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(stageA);

            // Wait for child to start
            childExecution.get();

            // Wait for the stageA to complete
            Promise.allOf(resultStageA).get();

            return new StageResult("stageB");
        }

        @Override
        public void manualVerificationStageA(StageA.VerificationStageARequest request) {

            Workflow.newExternalWorkflowStub(StageA.class,
                            StageA.buildWorkflowId(getWorkflowId()))
                    .manualVerificationStageA(request);
        }

        @Override
        public void manualVerificationStageB(StageB.VerificationStageBStatus verificationStageBStatus) {

            Workflow.newExternalWorkflowStub(StageB.class,
                            StageB.buildWorkflowId(getWorkflowId()))
                    .manualVerificationStageB(verificationStageBStatus);
        }
    }

    class OrchestratorRequest {

        private String data;

        public OrchestratorRequest() {
        }


        public OrchestratorRequest(String data) {
            this.data = data;
        }
    }
}

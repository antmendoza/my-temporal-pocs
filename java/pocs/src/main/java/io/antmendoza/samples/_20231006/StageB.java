package io.antmendoza.samples._20231006;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.temporal.workflow.*;
import org.slf4j.Logger;

@WorkflowInterface
public interface StageB {

  static String buildWorkflowId(String workflowId) {
    return "stageB-" + workflowId;
  }

  @WorkflowMethod
  StageResult run(StageBRequest stageBRequest);

  @SignalMethod
  void manualVerificationStageB(VerificationStageBStatus verificationStageBStatus);

  class StageBRequest {
    public final String id = "";

    public StageBRequest() {}
  }

  class StageBImpl implements StageB {

    private final Logger log = Workflow.getLogger("StageBImpl");

    private VerificationStageBStatus verificationStageBStatus;

    @Override
    public StageResult run(StageBRequest stageBRequest) {

      log.info("Starting with runId:" + Workflow.getInfo().getRunId());

      Workflow.await(() -> verificationStageBStatus != null);

      if (verificationStageBStatus.retryStage()) {
        Workflow.continueAsNew(
            StageB.class.getSimpleName(),
            ContinueAsNewOptions.newBuilder().build(),
            new StageBRequest());
      }

      return new StageResult(verificationStageBStatus);
    }

    @Override
    public void manualVerificationStageB(VerificationStageBStatus verificationStageBStatus) {
      this.verificationStageBStatus = verificationStageBStatus;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  class VerificationStageBStatus {
    public static final String RETRY_FROM_STAGE_A = "RETRY_FROM_STAGE_A";
    public static final String STATUS_OK = "STATUS_OK";
    public static final String STATUS_KO = "STATUS_KO";
    private String value;

    public VerificationStageBStatus() {}

    public VerificationStageBStatus(String value) {
      this.value = value;
    }

    @JsonIgnore
    public boolean isVerificationOk() {
      return this.value.equals(STATUS_OK);
    }

    @JsonIgnore
    public boolean retryStage() {
      return this.value.equals(STATUS_KO);
    }

    @JsonIgnore
    public boolean retryFromStageA() {
      return this.value.equals(RETRY_FROM_STAGE_A);
    }

    @Override
    public String toString() {
      return "VerificationStageBStatus{" + "value='" + value + '\'' + '}';
    }
  }

}

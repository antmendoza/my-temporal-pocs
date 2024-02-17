package io.antmendoza.samples._20231006;

public class StageResult {
    private String stage;

    private StageB.VerificationStageBStatus verificationStageBStatus;

    public StageResult() {
    }

    public StageResult(StageB.VerificationStageBStatus verificationStageBStatus) {
        this.verificationStageBStatus = verificationStageBStatus;
    }

    public StageResult(final String stageA) {
        this.stage = stageA;
    }

    public StageB.VerificationStageBStatus getVerificationStageBStatus() {
        return verificationStageBStatus;
    }


    public String getStage() {
        return stage;
    }
}

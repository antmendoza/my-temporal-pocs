package io.antmendoza.samples._20231006;

public class StageResult {
    private StageB.VerificationStageBStatus verificationStageBStatus;

    public StageResult() {
    }

    public StageResult(StageB.VerificationStageBStatus verificationStageBStatus) {
        this.verificationStageBStatus = verificationStageBStatus;
    }

    public StageB.VerificationStageBStatus getVerificationStageBStatus() {
        return verificationStageBStatus;
    }
}

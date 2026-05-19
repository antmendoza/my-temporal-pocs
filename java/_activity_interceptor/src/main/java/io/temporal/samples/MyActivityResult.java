package io.temporal.samples;

public class MyActivityResult {

    private boolean auditLoggingActivitySuccess;

    private String activityResult;


    public MyActivityResult() {

    }

    public MyActivityResult(boolean businessLoggingActivitySuccess, String activityResult) {
        this.auditLoggingActivitySuccess = businessLoggingActivitySuccess;
        this.activityResult = activityResult;
    }

    public MyActivityResult(String s) {
        this.activityResult = s;
    }

    public boolean isAuditLoggingActivitySuccess() {
        return auditLoggingActivitySuccess;
    }

    public String getActivityResult() {
        return activityResult;
    }

}

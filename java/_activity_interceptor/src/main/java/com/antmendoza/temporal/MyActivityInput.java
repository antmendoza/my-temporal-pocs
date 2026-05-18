package com.antmendoza.temporal;

public class MyActivityInput {


    private boolean trackAuditLogging;
    private String activityInput;


    public MyActivityInput() {

    }

    public MyActivityInput(boolean trackAuditLogging, String activityInput) {
        this.trackAuditLogging = trackAuditLogging;
        this.activityInput = activityInput;
    }

    public MyActivityInput(String s) {
        this.activityInput = s;
    }

    public boolean isTrackAuditLogging() {
        return trackAuditLogging;
    }

    public String getActivityInput() {
        return activityInput;
    }
}

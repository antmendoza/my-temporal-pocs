package com.antmendoza;


public class WorkflowInput {

    private String name;
    private int recordsToProcess;
    private int currentRecord;


    public WorkflowInput() {
    }

    public WorkflowInput(String name, int recordsToProcess, int currentRecord) {
        this.name = name;
        this.recordsToProcess = recordsToProcess;
        this.currentRecord = currentRecord;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setRecordsToProcess(int recordsToProcess) {
        this.recordsToProcess = recordsToProcess;
    }

    public void setCurrentRecord(int currentRecord) {
        this.currentRecord = currentRecord;
    }

    public String getName() {
        return name;
    }

    public int getRecordsToProcess() {
        return recordsToProcess;
    }

    public int getCurrentRecord() {
        return currentRecord;
    }

    //This does not work because the Function is not serializable, or create a custom converter
    //public boolean shouldCAN(Function<WorkflowInput, Boolean> can) {
    //    return Workflow.getInfo().isContinueAsNewSuggested();
    //}


}
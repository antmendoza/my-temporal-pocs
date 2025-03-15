package com.antmendoza.temporal.config;

public class FromEnv {
    private static String workerPort;
    private static String activitiesPerSecondPerTQ;
    private static String activityLatencyMs;

    public static String getWorkerPort() {
        return FromEnv.workerPort == null ? System.getenv("PQL_PORT") : FromEnv.workerPort;
    }

    public static String getActivitiesPerSecondPerTQ() {

        return FromEnv.activitiesPerSecondPerTQ == null ? System.getenv("MAX_TASKQUEUE_ACTIVITIES_PER_SECOND") : FromEnv.activitiesPerSecondPerTQ;

    }

    public static String getActivityLatencyMs() {

        return FromEnv.activityLatencyMs == null ? System.getenv("SLEEP_ACTIVITY_IN_MS") : FromEnv.activityLatencyMs;


    }

    public static Integer getConcurrentActivityExecutionSize() {
        final String concurrent_activity_execution_size = System.getenv("CONCURRENT_ACTIVITY_EXECUTION_SIZE");

        System.out.println(">>>>> " + concurrent_activity_execution_size);

        return concurrent_activity_execution_size == null ? 0 : Integer.parseInt(concurrent_activity_execution_size);
    }

    public static Integer getConcurrentWorkflowExecutionSize() {
        final String concurrent_workflow_execution_size = System.getenv("CONCURRENT_WORKFLOW_EXECUTION_SIZE");
        return concurrent_workflow_execution_size == null ? 0 : Integer.parseInt(concurrent_workflow_execution_size);
    }


    public static Integer getConcurrentWorkflowPollers() {
        final String value = System.getenv("CONCURRENT_WORKFLOW_POLLERS");
        return value == null ? 0 : Integer.parseInt(value);
    }

    public static Integer getConcurrentActivityPollers() {
        final String value = System.getenv("CONCURRENT_ACTIVITY_POLLERS");
        return value == null ? 0 : Integer.parseInt(value);
    }

    public static int getCacheSize() {
        final String cache_size = System.getenv("CACHE_SIZE");
        return cache_size == null ? 0 : Integer.parseInt(cache_size);
    }

    public static int getMaxWorkflowThreadCount() {
        final String cache_size = System.getenv("MAX_WORKFLOW_THREAD_COUNT");
        return cache_size == null ? 0 : Integer.parseInt(cache_size);
    }

    public FromEnv withWorkerPort(String workerPort) {
        FromEnv.workerPort = workerPort;
        return this;
    }

    public FromEnv withActivitiesPerSecondPerTQ(String actionsPerSecond) {

        FromEnv.activitiesPerSecondPerTQ = actionsPerSecond;
        return this;


    }

    public FromEnv withActivityLatencyMS(String activityLatency) {
        FromEnv.activityLatencyMs = activityLatency;
        return this;

    }

    public static boolean getDisableEagerDispatch() {
        return Boolean.valueOf(System.getenv("DISABLE_EAGER_DISPATCH"));
    }

}

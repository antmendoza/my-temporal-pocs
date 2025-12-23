package io.temporal.config;

public class FromEnv {
  private static String workerPort = "8071";
  private static String activitiesPerSecondPerTQ = "0";
  private static String activityLatencyMs = "1000";

  public static String getWorkerPort() {
    return System.getenv("PQL_PORT") == null ? FromEnv.workerPort : System.getenv("PQL_PORT");
  }

  public static String getActivitiesPerSecondPerTQ() {

    return FromEnv.activitiesPerSecondPerTQ == null
        ? System.getenv("MAX_TASKQUEUE_ACTIVITIES_PER_SECOND")
        : FromEnv.activitiesPerSecondPerTQ;
  }

  public static String getActivityLatencyMs() {

    return System.getenv("SLEEP_ACTIVITY_IN_MS") == null
        ? FromEnv.activityLatencyMs
        : System.getenv("SLEEP_ACTIVITY_IN_MS");
  }

  public static Integer getConcurrentActivityExecutionSize() {
    final String concurrent_activity_execution_size =
        System.getenv("CONCURRENT_ACTIVITY_EXECUTION_SIZE");

    System.out.println(">>>>> " + concurrent_activity_execution_size);

    return concurrent_activity_execution_size == null
        ? 0
        : Integer.parseInt(concurrent_activity_execution_size);
  }

  public static Integer getConcurrentWorkflowExecutionSize() {
    final String concurrent_workflow_execution_size =
        System.getenv("CONCURRENT_WORKFLOW_EXECUTION_SIZE");
    return concurrent_workflow_execution_size == null
        ? 0
        : Integer.parseInt(concurrent_workflow_execution_size);
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

  public static boolean isQueryCANWorkflow() {
    return false;
  }

  public static boolean fixedSlot() {

    return getWorkerTunner() != null && getWorkerTunner().equalsIgnoreCase("fixed-slot");
  }

  private static String getWorkerTunner() {
    final String workerTunner = System.getenv("WORKER_TUNNER");
    System.out.println(" WORKER_TUNNER >>>>> " + workerTunner);
    return workerTunner;
  }

  public static boolean fineTunner() {
    return getWorkerTunner() != null && getWorkerTunner().equalsIgnoreCase("fine-tunner");
  }

  public static boolean resourceBased() {
    return getWorkerTunner() != null && getWorkerTunner().equalsIgnoreCase("resource-based");
  }

  public static boolean getDisableEagerDispatch() {
    return Boolean.valueOf(System.getenv("DISABLE_EAGER_DISPATCH"));
  }
}

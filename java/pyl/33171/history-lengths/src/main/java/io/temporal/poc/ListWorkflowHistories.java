package io.temporal.poc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowExecutionMetadata;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Reads a workflow-list JSON export (one of the files in this ticket folder), and for each distinct
 * workflow id it lists all executions (runs) matching that id in the namespace, fetches each run's
 * history, and prints the history length (event count).
 *
 * Usage: java -jar history-lengths.jar <input.json> [config.properties]
 */
public class ListWorkflowHistories {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: ListWorkflowHistories <input.json> [config.properties]");
            System.exit(2);
        }
        Path inputJson = Path.of(args[0]);
        Path configPath = Path.of(args.length >= 2 ? args[1] : "config.properties");

        Properties cfg = loadConfig(configPath);
        String endpoint = required(cfg, "endpoint");
        String namespace = required(cfg, "namespace");
        String apiKey = firstNonBlank(System.getenv("TEMPORAL_API_KEY"), cfg.getProperty("apiKey"));
        if (isBlank(apiKey)) {
            throw new IllegalStateException("API key missing: set 'apiKey' in " + configPath + " or env TEMPORAL_API_KEY");
        }

        List<String> workflowIds = readWorkflowIds(inputJson);
        System.out.printf("Loaded %d distinct workflow ids from %s%n", workflowIds.size(), inputJson);
        System.out.printf("Connecting to %s namespace=%s%n%n", endpoint, namespace);

        WorkflowServiceStubs service = buildService(endpoint, namespace, apiKey);
        try {
            WorkflowClient client = WorkflowClient.newInstance(
                    service, WorkflowClientOptions.newBuilder().setNamespace(namespace).build());

            System.out.printf("%-40s  %-36s  %-14s  %s%n", "WORKFLOW_ID", "RUN_ID", "STATUS", "EVENTS");
            System.out.println("-".repeat(110));

            long totalRuns = 0;
            long notFound = 0;
            for (String wfId : workflowIds) {
                System.out.println("---------------------------- \n");

                List<WorkflowExecutionMetadata> runs = new ArrayList<>();
                String query = "WorkflowId = '" + wfId.replace("'", "''") + "'";


                client.listExecutions(query).forEach(runs::add);

                if (runs.isEmpty()) {
                    notFound++;
                    System.out.printf("%-40s  %-36s  %-14s  %s%n", wfId, "-", "-", "(no executions found)");
                    continue;
                }
                for (WorkflowExecutionMetadata run : runs) {
                    totalRuns++;

                    WorkflowExecution exec = run.getExecution();
                    String status = run.getStatus() != null ? run.getStatus().name() : "?";
                    long length = run.getWorkflowExecutionInfo().getHistoryLength();

                    //running workflows does not have history length Search Atrribute value
                    if (length == 0) {
                        length = historyLength(client, exec);
                    }
                    System.out.printf("%-40s  %-36s  %-14s  %d%n",
                            exec.getWorkflowId(), exec.getRunId(), status, length);
                }
            }

            System.out.println("-".repeat(110));
            System.out.printf("%d workflow ids -> %d runs fetched, %d ids with no executions%n",
                    workflowIds.size(), totalRuns, notFound);
        } finally {
            service.shutdownNow();
            service.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    /**
     * Fetch the full history for a run through the client (so the temporal-namespace header and
     * API-key auth are attached) and return the event count. fetchHistory() pages internally.
     */
    private static long historyLength(WorkflowClient client, WorkflowExecution exec) {
        return client.fetchHistory(exec.getWorkflowId(), exec.getRunId()).getEvents().size();
    }

    private static WorkflowServiceStubs buildService(String endpoint, String namespace, String apiKey) {
        // API-key auth to Temporal Cloud: bearer token via addApiKey over TLS. The namespace is
        // supplied on WorkflowClientOptions; the SDK routes the temporal-namespace header from it.
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(endpoint)
                        .setEnableHttps(true)
                        .addApiKey(() -> apiKey)
                        .build());
    }

    private static List<String> readWorkflowIds(Path inputJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputJson.toFile());
        JsonNode workflows = root.path("workflows");
        if (!workflows.isArray()) {
            throw new IllegalArgumentException("Expected a top-level 'workflows' array in " + inputJson);
        }
        Set<String> ids = new LinkedHashSet<>();
        for (JsonNode wf : workflows) {
            JsonNode id = wf.get("id");
            if (id != null && !id.isNull()) {
                ids.add(id.asText());
            }
        }
        return new ArrayList<>(ids);
    }

    private static Properties loadConfig(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IllegalStateException("Config file not found: " + path
                    + " (copy config.properties.example to config.properties)");
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(path.toFile())) {
            props.load(in);
        }
        return props;
    }

    private static String required(Properties cfg, String key) {
        String v = cfg.getProperty(key);
        if (isBlank(v)) {
            throw new IllegalStateException("Missing required config property: " + key);
        }
        return v.trim();
    }

    private static String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : b;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

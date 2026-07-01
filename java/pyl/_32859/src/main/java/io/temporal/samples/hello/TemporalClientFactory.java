package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class TemporalClientFactory {

    private TemporalClientFactory() {}

    static WorkflowClient newClient() {
        Properties props = load();
        boolean local = Boolean.parseBoolean(props.getProperty("temporal_local_server", "true"));
        String namespace = props.getProperty("temporal_namespace", "default");
        String target = props.getProperty("temporal_starter_target_endpoint", "localhost:7233");

        WorkflowServiceStubs service;
        if (local) {
            System.out.println("Connecting to local Temporal server");
            service = WorkflowServiceStubs.newLocalServiceStubs();
        } else {
            String apiKey = System.getenv("TEMPORAL_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException(
                        "TEMPORAL_API_KEY environment variable is required when temporal_local_server=false");
            }
            System.out.println("Connecting to Temporal Cloud: " + target + " (namespace " + namespace + ")");
            service =
                    WorkflowServiceStubs.newServiceStubs(
                            WorkflowServiceStubsOptions.newBuilder()
                                    .setTarget(target)
                                    .setEnableHttps(true)
                                    .addApiKey(() -> apiKey)
                                    .build());
        }

        return WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder().setNamespace(namespace).build());
    }

    private static Properties load() {
        try (InputStream in =
                TemporalClientFactory.class.getClassLoader().getResourceAsStream("temporal.properties")) {
            if (in == null) {
                throw new IllegalStateException("temporal.properties not found on classpath");
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
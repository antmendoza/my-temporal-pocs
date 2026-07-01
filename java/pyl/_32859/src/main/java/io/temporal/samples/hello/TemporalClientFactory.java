package io.temporal.samples.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class TemporalClientFactory {

    private TemporalClientFactory() {
    }

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
            WorkflowServiceStubsOptions.Builder builder =
                    WorkflowServiceStubsOptions.newBuilder()
                            .setTarget(target)
                            .setEnableHttps(true)
                            .addApiKey(() -> apiKey);


            boolean disableGzip = Boolean.parseBoolean(System.getenv("DISABLE_GZIP"));
            if (disableGzip) {
                disableGzipIfSupported(builder);
            }


            service = WorkflowServiceStubs.newServiceStubs(builder.build());
        }

        return WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder().setNamespace(namespace).build());
    }

    private static void disableGzipIfSupported(WorkflowServiceStubsOptions.Builder builder) {
        try {
            Class<?> compression = Class.forName("io.temporal.serviceclient.GrpcCompression");
            Object gzip = Enum.valueOf((Class) compression, "NONE");
            builder.getClass().getMethod("setGrpcCompression", compression).invoke(builder, gzip);
            System.out.println("gRPC gzip compression enabled");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("gRPC compression not supported by this SDK version; skipping");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to enable gRPC compression", e);
        }
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
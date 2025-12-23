package com.antmendoza.temporal.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TemporalProperties {


    private boolean temporalLocalServer;
    private String temporalKeyLocation;
    private String temporalCertLocation;
    private String temporalNamespace = "default";
    private String temporalWorkerTargetEndpoint = "localhost:7233";
    private String temporalStarterTargetEndpoint = "localhost:7233";

    public TemporalProperties() {

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("temporal.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);


            this.temporalLocalServer = Boolean.parseBoolean(prop.getProperty("temporal_local_server"));


            System.out.println(">>>>> temporalServerLocalhost: " + prop.getProperty("temporal_local_server"));
            System.out.println(">>>>> temporalServerLocalhost: " + this.temporalLocalServer);

            if (prop.getProperty("temporal_namespace") != null) {
                this.temporalNamespace = prop.getProperty("temporal_namespace");
            }


            if (prop.getProperty("temporal_worker_target_endpoint") != null) {
                this.temporalWorkerTargetEndpoint = prop.getProperty("temporal_worker_target_endpoint");
            }


            if (prop.getProperty("temporal_starter_target_endpoint") != null) {
                this.temporalStarterTargetEndpoint = prop.getProperty("temporal_starter_target_endpoint");
            }

            if (!this.temporalLocalServer) {

                this.temporalKeyLocation = prop.getProperty("temporal_key_location");
                this.temporalCertLocation = prop.getProperty("temporal_cert_location");
            }

        } catch (IOException ex) {

            new RuntimeException(ex);
        }

    }


    public String getTemporalKeyLocation() {
        return temporalKeyLocation;
    }

    public String getTemporalCertLocation() {
        return temporalCertLocation;
    }

    public String getTemporalNamespace() {
        return temporalNamespace;
    }

    public String getTemporalWorkerTargetEndpoint() {
        return temporalWorkerTargetEndpoint;
    }

    public String getTemporalStarterTargetEndpoint() {
        return temporalStarterTargetEndpoint;
    }

    public boolean isTemporalLocalServer() {
        return temporalLocalServer;
    }
}

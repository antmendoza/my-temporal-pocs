package io.temporal.samples.earlyreturn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TemporalProperties {

  private boolean temporalLocalServer = false;
  private String temporalKeyLocation;
  private String temporalCertLocation;
  private String temporalNamespace = "default";
  private String temporalTargetEndpoint = "localhost:7233";

  public TemporalProperties() {

    try (InputStream input =
        getClass().getClassLoader().getResourceAsStream("temporal.properties")) {

      Properties prop = new Properties();

      // load a properties file
      prop.load(input);

      if (Boolean.parseBoolean(prop.getProperty("temporal_local_server"))) {
        this.temporalLocalServer = true;
        return;
      }

      if (prop.getProperty("temporal_namespace") != null) {
        this.temporalNamespace = prop.getProperty("temporal_namespace");
      }

      if (prop.getProperty("temporal_worker_target_endpoint") != null) {
        this.temporalTargetEndpoint = prop.getProperty("temporal_worker_target_endpoint");
      }

      this.temporalKeyLocation = prop.getProperty("temporal_key_location");
      this.temporalCertLocation = prop.getProperty("temporal_cert_location");

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

  public String getTemporalTargetEndpoint() {
    return temporalTargetEndpoint;
  }

  public boolean isTemporalLocalServer() {
    return temporalLocalServer;
  }
}

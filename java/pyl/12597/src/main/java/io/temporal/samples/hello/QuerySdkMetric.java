package io.temporal.samples.hello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QuerySdkMetric {
  private final String metricsUrl;
  private final String metricName;
  private final String metricFilter;

  public QuerySdkMetric() {

    this(
        "http://localhost:8078/metrics",
        "temporal_worker_task_slots_available",
        "worker_type=\"LocalActivityWorker\"");
  }

  public QuerySdkMetric(String metricsUrl, String metricName, String metricFilter) {
    this.metricsUrl = metricsUrl;
    this.metricName = metricName;
    this.metricFilter = metricFilter;
  }

  public QuerySdkMetric(String metricName, String metricFilter) {

    this("http://localhost:8078/metrics", metricName, metricFilter);
  }

  public double fetchMetric() throws RuntimeException {
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(metricsUrl).openConnection();

      conn.setConnectTimeout(500);
      conn.setReadTimeout(500);

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

        return reader
            .lines()
            .filter(l -> l.startsWith(metricName))
            .filter(l -> l.contains(metricFilter))
            .map(l -> l.substring(l.lastIndexOf(' ') + 1))
            .mapToDouble(Double::parseDouble)
            .findFirst()
            .orElse(Double.NaN);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

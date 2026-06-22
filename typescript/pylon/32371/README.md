# pylon-32371

TypeScript SDK worker exporting metrics via OTLP to an OpenTelemetry Collector,
scraped by Prometheus and visualised in Grafana with the official
[temporal-core-sdks-otel](https://github.com/temporalio/dashboards/blob/master/sdk/temporal-core-sdks-otel.json)
dashboard.

```
worker (host) --OTLP/gRPC:4317--> otel-collector --:8889/metrics--> prometheus --> grafana :3000
```

## Layout

```
src/                            TS SDK worker + workflow + client
observability/
  docker-compose.yml            collector + prometheus + grafana
  collector.yaml                OTLP in, Prometheus out (add_metric_suffixes: false)
  prometheus/config.yml         scrapes otel-collector:8889
  grafana/provisioning/
    datasources/prometheus.yml
    dashboards/all.yml
    dashboards/temporal-core-sdks-otel.json
```

## Run

1. Start the observability stack:

   ```sh
   cd observability
   docker compose up
   ```

   - Grafana:   http://localhost:3000 (anonymous Admin)
   - Prometheus: http://localhost:9090
   - Collector Prometheus exporter: http://localhost:8889/metrics

2. Start Temporal dev server in another shell:

   ```sh
   temporal server start-dev
   ```

3. Install deps and start the worker:

   ```sh
   npm install
   npm run start.watch
   ```

4. Drive some workflow traffic:

   ```sh
   npm run workflow
   ```

   Open the **Temporal SDK** dashboard in Grafana — metrics like
   `temporal_workflow_completed`, `temporal_request`, and
   `temporal_workflow_endtoend_latency_bucket` should populate within a few seconds.

## Notes

- The collector's Prometheus exporter is configured with `add_metric_suffixes: false`
  so counter names stay as `temporal_request` (not `temporal_request_total`) to match
  the dashboard PromQL.
- The dashboard's `datasource` template variable auto-binds to the provisioned
  Prometheus datasource — no manual selection needed.
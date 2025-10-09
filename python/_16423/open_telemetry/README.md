# OpenTelemetry Sample

This sample shows how to configure OpenTelemetry to capture workflow traces. This variant sends traces to Jaeger.

For this sample, the optional `open_telemetry` dependency group must be included. To include, run:
```bash


    uv python install 3.13

    uv sync

    uv sync --group open-telemetry
```

To run, first see [README.md](../README.md) for prerequisites. Then start a local Jaeger instance (with OTLP ingest enabled):
```bash
    docker compose up
```
Now, start the worker in its own terminal:
```bash
    uv run worker.py
```
Then, in another terminal, run the following to execute the workflow:
```bash
    uv run starter.py
```
The workflow should complete with the hello result.

Open Jaeger UI at http://localhost:16686 and search for your service (e.g., `my-service`) to view traces. Note that Jaeger does not display SDK metrics; this sample disables metrics when using Jaeger directly.

Note, in-workflow spans do not have a time associated with them. This is by intention since due to replay. In OpenTelemetry, only the process that started the span may end it. But in Temporal a span may cross workers/processes. Therefore we intentionally start-then-end in-workflow spans immediately. So while the start time and hierarchy is accurate, the duration is not.

The Python SDK’s tracing behavior is designed to avoid trace inconsistencies and orphaned spans due to the mismatch between Temporal’s execution model and OpenTelemetry’s span lifecycle requirements. If you want to see workflow-level spans, ensure your workflows are started by a client with the tracing interceptor, or use the new always_create_workflow_spans option if you accept the trade-offs.

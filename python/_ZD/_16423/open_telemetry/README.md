# OpenTelemetry Sample

This example shows how to emit custom spans that last the entire workflow execution. 

Note that the recommended way to emmit spans is using `otel_workflow` helper, and temporal natively does not support spans that last the entire workflow execution.
- [core-sdk docs](https://ruby.temporal.io/index.html#label-OpenTelemetry+Tracing+in+Workflows)
- [python-sdk docs](https://github.com/temporalio/sdk-python/blob/cde3427282131054ac774466aaf146ed89a7d8a2/temporalio/contrib/opentelemetry.py#L705-L713)


## install dependencies

```bash


    uv python install 3.13

    uv sync

    uv sync --group open-telemetry
```

## Start jeager
```bash
    docker compose up
```

## Now, start the worker in its own terminal:
```bash
    uv run worker.py
```
## Then, in another terminal, run the following to execute the workflow:
```bash
    uv run starter.py
```

## Results
The workflow should complete with the hello result.

Open Jaeger UI at http://localhost:16686 and search for your service (e.g., `my-service`) to view traces. 

![Screenshot 2025-10-13 at 10.26.48.png](Screenshot%202025-10-13%20at%2010.26.48.png)



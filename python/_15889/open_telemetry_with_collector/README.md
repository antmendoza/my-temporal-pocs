# OpenTelemetry Sample


``` bash
poetry install --with open_telemetry
```

Before starting the collector, edit the [collector.yaml](collector%2Fcollector.yaml) file to add the dd api_key


```bash
docker compose down --remove-orphans && docker volume prune -f

docker-compose up 

```

### Otel / Datadog




Start one worker

``` bash
ps aux | grep worker.py

pkill -f worker.py
export WORKER_ID=100_WORKER___1 
poetry run python3 worker.py
```
export PROMETHEUS_PORT=9091



Start 50K workflows

``` bash
export WORKER_ID=CLIENT

# Set the number of workflows to run
export WORKFLOW_COUNT=20000
poetry run python3 starter.py
```


## references
https://docs.datadoghq.com/opentelemetry/guide/otlp_delta_temporality/



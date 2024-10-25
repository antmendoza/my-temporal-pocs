# OpenTelemetry - .NET Metrics

```bash
docker network rm temporal_network
docker network create -d bridge  temporal_network

```

```bash
cd opentelemetry-collector-contrib
docker-compose down -v
docker-compose up 

```

```bash
cd grafana
docker compose down --remove-orphans
docker compose up --remove-orphans

```

Then, run the following from this directory in a separate terminal to start the worker:

```bash
    dotnet run worker
```

Then in another terminal, run the workflow stfrom this directory:

```bash
    dotnet run workflow
```


rate(  temporal_workflow_task_replay_latency_milliseconds_count [4h])
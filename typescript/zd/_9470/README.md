# Traces to DD


Before starting the collector, edit the [collector.yaml](collector%2Fcollector.yaml) file to add the dd api_key


```bash

cd collector

docker compose down --remove-orphans && docker volume prune -f

docker-compose up 

```

### Running this sample

1. `temporal server start-dev` to start [Temporal Server](https://github.com/temporalio/cli/#installation).
1. `npm install` to install dependencies.
1. `npm run start.watch` to start the Worker.
1. In another shell, `npm run workflow` to run the Workflow.


#### Metrics

Go to https://app.datadoghq.com/metric/explorer and add the following query
```
sum:temporal_request{*} by {operation}.as_rate()
```

![](img.png)


#### Traces

https://app.datadoghq.com/apm/traces

![Screenshot 2025-02-04 at 14.28.51.png](Screenshot%202025-02-04%20at%2014.28.51.png)


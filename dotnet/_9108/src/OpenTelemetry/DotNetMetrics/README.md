# OpenTelemetry - .NET Metrics

```bash
docker network rm temporal_network
docker net




work create -d bridge  temporal_network

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

**temporal_workflow_task_replay_latency_milliseconds_count**
http://localhost:9090/graph?g0.expr=temporal_workflow_task_replay_latency_milliseconds_count&g0.tab=1&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h


**temporal_activity_schedule_to_start_latency_milliseconds_count**
http://localhost:9090/graph?g0.expr=temporal_activity_schedule_to_start_latency_milliseconds_count&g0.tab=1&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h






## Test scenario 
### SDK dotned
- cache-size 500
- workflow implementation

![Screenshot 2024-10-29 at 13.28.34.png](Screenshot%202024-10-29%20at%2013.28.34.png)

- schedule 100 workflow execution
- run worker
    - after workflows goes to sleep stop the worker
- query temporal_workflow_task_replay_latency_milliseconds_count **My expectation is this to be 0**

![Screenshot 2024-10-25 at 16.02.43.png](Screenshot%202024-10-25%20at%2016.02.43.png)
- 
- run the worker again, the workflows will be replayed after the timer is triggered

**My expectation is this to be 100**
![Screenshot 2024-10-25 at 16.08.31.png](Screenshot%202024-10-25%20at%2016.08.31.png)


like activity schedule to start count 

![Screenshot 2024-10-25 at 16.06.46.png](Screenshot%202024-10-25%20at%2016.06.46.png)


### SDK JAVA
- cache-size 500
- workflow implementation

![Screenshot 2024-10-29 at 13.27.49.png](Screenshot%202024-10-29%20at%2013.27.49.png)
- 
- 
- schedule 100 workflow execution
- run worker
    - after workflows goes to sleep stop the worker
- query temporal_workflow_task_replay_latency_seconds_count My expectation is this to be 0

![Screenshot 2024-10-29 at 13.18.19.png](Screenshot%202024-10-29%20at%2013.18.19.png)

- run the worker again, the workflows will be replayed after the timer is triggered

![Screenshot 2024-10-29 at 13.20.07.png](Screenshot%202024-10-29%20at%2013.20.07.png)

- like activity schedule to start count 

![Screenshot 2024-10-29 at 13.22.38.png](Screenshot%202024-10-29%20at%2013.22.38.png)
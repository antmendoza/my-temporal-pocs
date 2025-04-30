# Blocking Event Loop Example

This example demonstrates how blocking the event loop in a worker can prevent the worker from processing workflow tasks.

## Install dependencies


```bash
uv python install 3.13
uv sync
uv sync --group encryption
```



## running the example


### start grafana and prometheus

```
./start-grafana_prometheus.sh
```
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090


### run the worker

```bash
# export RUST_LOG="temporal_sdk_core=DEBUG"
#TEMPORAL_DEBUG=true 
uv run _worker.py
```

### start workflows

```bash
# export RUST_LOG="temporal_sdk_core=DEBUG"
#TEMPORAL_DEBUG=true 
uv run _starter.py
```


#### There is a codec with function `sleep` that "randomly" blocks the asyncio 

This prevents the worker for start workflow task to the point that they timesout (default timeout=10)

We can see it in worker logs
```
2025-04-30T14:18:45.415499Z  WARN temporal_sdk_core::worker::workflow: Task not found when completing error=status: NotFound, message: "Workflow task not found.",...
```
and grafana dashboard (**Workflow Task Execution Latency (p95)**)

![Screenshot 2025-04-30 at 16.21.36.png](Screenshot%202025-04-30%20at%2016.21.36.png)

The task is dispatched to the worker, but it can not be run it while the event loop is blocked


After changing the code to run with asyncio the performance is much better, in the graph below the 
change was done at 14:23: 

- workflow execution latency went from 3 minutes to  8 seconds (p95)
- workflow task latency went from 1 minute to 7 seconds (p95)
    - still high due to the latency added by the codec, but it is not blocking the worker in the same way.
- workflow schedule to start latency went from 1 minute to a few milliseconds (p95) 

To change the code to run with asyncio you can search for `# Simulate blocking work` and replace 
```
        #asyncio.get_running_loop().run_in_executor(None, sleep)
        sleep()
```
for
```
        asyncio.get_running_loop().run_in_executor(None, sleep)
        #sleep()
```

then restart/rerun everything to review the performance

![Screenshot 2025-04-30 at 16.24.18.png](Screenshot%202025-04-30%20at%2016.24.18.png)


####  Changing the code to run purely with asyncio, makes the real difference

```
        #asyncio.get_running_loop().run_in_executor(None, sleep)
        await asyncio.sleep(0.4)
        #sleep()
```

but it is not always possible due to external dependencies.
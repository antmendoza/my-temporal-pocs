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




### Start one worker

``` bash
ps aux | grep worker.py

pkill -f worker.py
export WORKER_ID=100_WORKER___1 
poetry run python3 worker.py
```


### Start x workflows

``` bash
export WORKER_ID=CLIENT

# Set the number of workflows to run
export WORKFLOW_COUNT=1
poetry run python3 starter.py
```


## View logs in Datadog


### file based logs

![Screenshot 2025-09-18 at 13.03.28.png](Screenshot%202025-09-18%20at%2013.03.28.png)

 each individual log entry

![Screenshot 2025-09-18 at 13.03.21.png](Screenshot%202025-09-18%20at%2013.03.21.png)



### otel configuration
![Screenshot 2025-09-18 at 11.50.56.png](Screenshot%202025-09-18%20at%2011.50.56.png)


![Screenshot 2025-09-18 at 11.51.17.png](Screenshot%202025-09-18%20at%2011.51.17.png)


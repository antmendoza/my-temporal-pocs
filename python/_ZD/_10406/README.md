# OpenTelemetry Sample


``` bash
poetry install --with open_telemetry
```

Before starting the collector, edit the [collector.yaml](collector%2Fcollector.yaml) file to add the dd api_key


```bash

cd collector

docker compose down --remove-orphans && docker volume prune -f

docker-compose up 

```


Start the worker

``` bash
poetry run python3 worker.py
```

Start the starter

``` bash
poetry run python3 starter.py
```


Core-based SDKs: Metrics of the type Histogram are measured in milliseconds by default.


![Screenshot 2025-01-29 at 13.07.19.png](Screenshot%202025-01-29%20at%2013.07.19.png)

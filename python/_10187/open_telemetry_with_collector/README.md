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

Start the starter new code (same result)

``` bash
poetry run python3 starter_new_code.py
```

![Screenshot 2025-01-30 at 16.30.21.png](Screenshot%202025-01-30%20at%2016.30.21.png)

# Example to push metrics and traces to datadog

## Collector configuration

- configure `YOUR_API_KEY` in [collector.yaml](collector/collector.yaml) and modify the pipelines according your needs

#### Start the collector 

``` bash
cd collector
docker compose down --remove-orphans && docker volume prune -f
docker-compose up 
```


#### Start the worker and starter
- [Starter.java](src/main/java/com/antmendoza/opentelemetry/Starter.java)
- [TracingWorker.java](src/main/java/com/antmendoza/opentelemetry/TracingWorker.java)

By default, if you have not disabled the logging exporter in the traces pipeline, the collector should start printing traces.

![img.png](img.png)



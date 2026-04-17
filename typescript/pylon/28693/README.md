# Custom Gauge and Counter Metrics

Before starting the collector: 
- create a .env file in the collector folder with the DD_API_KEY


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


#### Activity implementation

Use retry values to create a custom metric: 

```
  Context.current().metricMeter.createGauge('my_custom_metric_gauge').set(Context.current().info.attempt);
  Context.current().metricMeter.createCounter('my_custom_metric_counter').add(Context.current().info.attempt);

```

#### Custom Metrics

Go to https://app.datadoghq.com/metric/explorer?exp_metric=my_custom_metric_gauge

```
avg:my_custom_metric_gauge{*}
```

![Screenshot 2026-04-17 at 17.06.20.png](Screenshot%202026-04-17%20at%2017.06.20.png)


Go to https://app.datadoghq.com/metric/explorer?exp_metric=my_custom_metric_counter

```
avg:my_custom_metric_counter{*}
```

![Screenshot 2026-04-17 at 17.06.27.png](Screenshot%202026-04-17%20at%2017.06.27.png)

## activity_execution_failed

![Screenshot 2026-04-16 at 14.49.46.png](Screenshot%202026-04-16%20at%2014.49.46.png)

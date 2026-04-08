### Steps to run this sample:
1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).


2) Start opentelemetry collector and prometheus

```bash
cd opentelemetry-collector-contrib
docker-compose up 
```



3) Start the worker

```bash
go run worker/main.go
```

4) query `temporal_worker_task_slots_available` in prometheus 

[http://localhost:9090/....](http://localhost:9090/graph?g0.expr=temporal_worker_task_slots_available&g0.tab=1&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h&g1.expr=&g1.tab=1&g1.stacked=0&g1.show_exemplars=0&g1.range_input=1h)


5) stop and clean docker-compose 

```bash
cd opentelemetry-collector-contrib
docker-compose down -v
```

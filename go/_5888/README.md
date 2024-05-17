### Steps to run this sample:
1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).

2) Run the following command to start the worker

```bash
go run worker/main.go
```
3) 
4) Run the following command to start the example


```bash
go run starter/main.go
```

4) Check metrics at http://localhost:9090/metrics (this is where the Prometheus agent scrapes it from).


```bash
cd opentelemetry-collector-contrib
docker-compose up 
```

```bash
cd opentelemetry-collector-contrib
docker-compose down -v
```

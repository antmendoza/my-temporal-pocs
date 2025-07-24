### Steps to run this sample:
1) Configure a [Temporal Server](https://github.com/temporalio/samples-go/tree/main/#how-to-use) (such as Temporal Cloud) with mTLS.

2) Run the following command to start the worker
```bash
go run worker/main.go
```
3) Run the following command to start the example
```bash
go run starter/main.go
```


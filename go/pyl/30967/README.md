### Steps to run this sample
1) Run a [Temporal server](https://github.com/temporalio/samples-go/tree/main/#how-to-use).


2) Start the worker
```bash
go run helloworld/worker/main.go
```

3) Start the workflow
```bash
go run helloworld/starter/main.go
```

## Output

The expectation is that the workflow execute Activity in the package `activity_v1`, 
it instead executes Activity in the package `activity_v2`.




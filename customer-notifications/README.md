### Steps to run this sample:


1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).


# Build the binary
```bash
make build
```

# Run the workflow
```bash
make run workflow
```

# Run the example
```bash
make run-example
```

2) 
```bash
go mod tidy
```


3) Run the following command to start the worker X 2
```bash
go run worker/main.go 
```


4) Run the following command to start the example
```bash
go run client/main.go

```


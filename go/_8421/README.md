Anonymous struct as schedule input 

Steps to run this sample:
1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).
2) Run 
``` bash
go run worker/main.go 
```
to start worker for schedule workflow.
3) Run
``` bash
go run starter/main.go
```
to start a schedule to run a workflow every second.

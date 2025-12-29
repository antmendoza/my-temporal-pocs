# Worker reporting negative count of slots available for local activities

This example tries to reproduce the scenario where a worker reports a negative count of slots available for local activities. 

The main logic in the [HelloActivity.java](src/main/java/io/temporal/samples/hello/HelloActivity.java) class 
- start a prometheus server that exposes metrics (method startPrometheusEndpoint)
- query periodically the number of available and used slots reported by the worker and print them if the value is < 0 (method queryLocalActivityMetrics)
- start a worker with a fixed number of local activity slots between 20 and 70, the worker is restarted every 4 seconds (method startAndRestartWorker)
- run a workflow that schedules concurrent local activities, up to 45 (method startWorkflow)



## run the example

```bash
./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.hello.HelloActivity"
```

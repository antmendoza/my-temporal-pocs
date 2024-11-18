
# SpringBoot configuration for open tracing



## Start service 1

Service 1 send a request to service 2


```bash
cd service-1

./mvnw compile exec:java -Dexec.mainClass=com.example.service1.Service1Application
```

## Start service 2

Service 2 receives the request and start a workflow. The workflow implementations contains an activity that
returns the traceId

```bash
cd service-2

./mvnw compile exec:java -Dexec.mainClass=com.example.service2.Service2Application
```

## Send request to service 1

```bash
curl http://localhost:8081/v1/test/tracing
```

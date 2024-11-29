# SpringBoot configuration for open tracing


#### Start the collector

```bash
cd collector

docker compose down --remove-orphans && docker volume prune -f

docker-compose up 
```



## Start service 1

Service 1 send a request to service 2

```bash
cd service-1

./mvnw compile exec:java -Dexec.mainClass=com.example.service1.Service1Application
```

## service 2


### Start service 2

Service 2 receives the request and start a workflow and schedule an activity in service 3, TASK_QUEUE_3


```bash
cd service-2

./mvnw compile exec:java -Dexec.mainClass=com.example.service2.Service2Application
```

### OR Start service 2 with agent

```bash
cd service-2_lazy

export MAVEN_OPTS=-javaagent:../opentelemetry-javaagent.jar
export OTEL_PROPAGATORS="tracecontext"

./mvnw compile exec:java -Dexec.mainClass=com.example.service2.Service2Application \
-Dotel.javaagent.configuration-file=../otelagent-config.properties \
-Dotel.instrumentation.micrometer.enabled=true \
-Dmetrics.enabled=true

```


#### OR Start service 2 with Temporal configuration 

```bash
cd service-springboot

./mvnw compile exec:java -Dexec.mainClass=com.example.servicespringboot.ServiceSpringApplication

```

## Send request to service 1

```bash
curl http://localhost:8081/v1/test/tracing
```


- https://opentelemetry.io/docs/languages/sdk-configuration/general/#otel_traces_exporter
- https://github.com/temporalio/sdk-java/tree/master/temporal-opentracing

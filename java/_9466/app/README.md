



``` bash
mvn compile exec:java -Dexec.mainClass="com.temporal.demos.temporalspringbootdemo.TemporalSpringbootDemoApplication"
```



``` bash
curl --header "Content-Type: application/json" \
--request POST \
http://localhost:3030/start
```

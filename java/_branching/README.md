# Run parallel branches

This sample shows how to run parallel branches in Temporal.

### Run the code

- Run the worker
```bash
./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.MyWorker" 
```


- Run the starter
```bash
./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.MyStarter" 
```



#### Expected behaviour
The branches will unblock once they receive the signal for the given jobId or the timeout is reached.

If the timeout is reached, the activity is retried and the function/branch returns false

##### Output

With the current implementation, see [MyStarter.java](src/main/java/io/temporal/samples/MyStarter.java), 
the jobId "job3" is never received, the activity for that jobId is retried in code and the function returns false.

```
[
  {
    "jobId": "job1",
    "result": true
  },
  {
    "jobId": "job2",
    "result": true
  },
  {
    "jobId": "job3",
    "result": false
  }
]
```

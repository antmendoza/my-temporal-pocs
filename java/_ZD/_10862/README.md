
```bash
./mvnw clean test
```


```

Results :

Tests in error: 
  replayWorkflowExecution_with_HelloActivityV3_add_list_strings(io.temporal.samples.hello.HelloActivityReplayTest): query failure for workflow_id: "workflow_id_in_replay"(..)

Tests run: 4, Failures: 0, Errors: 1, Skipped: 0



...


Caused by: java.lang.ClassCastException: class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl cannot be cast to class java.lang.Class (sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl and java.lang.Class are in module java.base of loader 'bootstrap')
        at io.temporal.common.converter.DataConverter.fromPayloads(DataConverter.java:162)
        at io.temporal.internal.sync.POJOWorkflowImplementationFactory$POJOWorkflowImplementation.execute(POJOWorkflowImplementationFactory.java:349)
        at io.temporal.internal.sync.WorkflowExecutionHandler.runWorkflowMethod(WorkflowExecutionHandler.java:71)

```
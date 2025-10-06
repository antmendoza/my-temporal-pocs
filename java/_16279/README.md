# Context propagation in Java



- Implement the context propagator to read and write the context:
  - [HelloActivity.java](src/main/java/io/temporal/samples/hello/HelloActivity.java#L108)




- Create the client and set the context propagator:
  
```
WorkflowClient client =
        WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder()
                        .setContextPropagators(Collections.singletonList(new MDCContextPropagator()))
                        .build());

```

- Before starting a workflow, set the MDC context:

```
    MDC.put("my-context", "hello from context propagator");        
```
        

- Activity returns whatever is in the context:

```
  @Override
  public String extractContext() {
      return MDC.get("my-context");
  }
```


- [Example of the generated workflow history](0199b89c-3f5e-74be-86ac-4c1df9ad2128_events.json)
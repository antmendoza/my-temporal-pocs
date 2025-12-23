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
    MDC.put("my-context-from-client", "hello my-context-from-client");
```


- Inside the workflow, set another value in the MDC context:
```
  MDC.put("my-context-from-workflow", "hello my-context-from-workflow");
```


- Activity returns whatever is in the context:

```
  @Override
  public String extractContext() {
      return MDC.get("my-context-from-client") + " | " + MDC.get("my-context-from-workflow");
  }
```


- Output when running the workflow:

```
  hello my-context-from-client | hello my-context-from-workflow

```


- [Example of the generated workflow history](019b322c-8926-73ad-b433-3550f6a7c253_events.json)
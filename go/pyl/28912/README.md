# update-with-start-interceptor

Reproduces an asymmetry in `ClientOutboundInterceptor` between
`SignalWithStartWorkflow` and `UpdateWithStartWorkflow`.

## The Issue

`ClientSignalWithStartWorkflowInput` exposes `Options *StartWorkflowOptions`
directly, so an interceptor can modify the start options (task queue, memo, etc.)
before the call reaches the server.

`ClientUpdateWithStartWorkflowInput` instead holds start parameters inside a
`client.WithStartWorkflowOperation` interface that has no method to read or
modify the underlying `StartWorkflowOptions`. There is no supported way for an
interceptor to customize them.



## Reproduction

```bash
go test ./update-with-start-interceptor/...
```

The test fails, demonstrating that the task queue override applied by the
interceptor has no effect on `UpdateWithStartWorkflow`.

# Ticket 33529

Kotlin `Async.function`/`Async.procedure` with a stub **method reference** spawns a `WorkflowThread`
per call (thread-pool exhaustion) because `MethodReferenceDisassembler.isAsync` doesn't recognize the
invokedynamic-lowered Kotlin method reference. Java behaves correctly.

- Repro + run instructions: `code/README.md`.
- Full analysis + shared root cause with ticket 33996:
  `../../../../projects/my-temporal-pocs/notes-for-tickets/33529.md`.
- SDK checkout for the fix: `~/dev/temporal/_java/sdk-java`.

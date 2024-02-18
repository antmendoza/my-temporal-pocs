```
// Make sure Request is serializable to JSON (ie. no function, no promises, etc)
type Request = { ... }

// Entity workflow pattern with serialization of request
// (ie. only one request is processed at a time)
export async function myWorkflow(requests: Request[] = []): Promise<void> {
  setHandler(mySignal, (input: Request) => {
    requests.push(input);
    // Don't await here. Otherwise, the Workflow may complete before the promise completes.
  });

  while (!workflowInfo().continueAsNewSuggested) {
		const timeSinceStart = Date.now() - workflowInfo().runStartTime.getTime();
    const shouldProcessMore = await condition(() => requests.length > 0, ms('24h') - timeSinceStart);
    if (!shouldProcessMore) return;

    const request = requests.shift();

    // Process request as appropriate
    await handleSingleRequest(request);
  }

  // Huge histories are bad for performance, so we switch to a new workflow execution whenever
  // history grows over 2000 events. When that happens, we forward any outstanding requests to the
  // next execution.
  await continueAsNew(requests);
}

function handleSingleRequest(request: Request): Promise<void> {
  // It's ok to await here
}

```
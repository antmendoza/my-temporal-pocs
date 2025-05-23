# Pause and retry activities

## Install dependencies


```bash
uv python install 3.13
uv sync
uv sync --group encryption
```

## running the example


### run the worker

```bash
uv run _worker.py
```

### start workflows

```bash
uv run _starter.py
```


Hi Chad, Dan, for code sample I am working on I need to capture 
when the activity execution (after retries/scheduleTo**Timeout etc..). 
Is there a way to do it within the WorkflowOutboundInterceptor? 
This method returns a handler I guess I need to intercept 



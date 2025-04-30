
## Install dependencies

```bash
uv python install 3.13
uv sync
uv sync --group encryption
```


## run the worker

```bash
# export RUST_LOG="temporal_sdk_core=DEBUG"
#TEMPORAL_DEBUG=true 
uv run _worker.py
```

## start workflows

```bash
# export RUST_LOG="temporal_sdk_core=DEBUG"
#TEMPORAL_DEBUG=true 
uv run _starter.py
```

## Implementation

The workflow execute activities sequentially. 

There is a codec that blocks the async function and waits for the result.

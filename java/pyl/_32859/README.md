# _32859

Cross-version replay check for the Temporal Java SDK.

`run.sh` builds two self-contained jars, each pinned to a different SDK version, then:

1. **WorkerMain** (`WORKER_VER`) runs the workflow to completion and exits.
2. **QueryClient** (`QUERY_VER`) starts its own worker and queries the workflow, so the
   query is answered by replaying the history on `QUERY_VER` — exercising cross-version replay.

## Run

Start a local Temporal dev server (in a separate terminal):

```bash
temporal server start-dev
```

Then run the script:

```bash
./run.sh
```

This builds both jars, runs the workflow on `WORKER_VER`, then queries/replays it on `QUERY_VER`.

On startup the script also registers a few search attributes (`MostRecentStartedActivity`,
`YourAttributeName`, `YourAttributeName_1..3`) via `temporal operator search-attribute create`.
These are idempotent — already-existing attributes are left as-is.

### Version overrides

Defaults are `WORKER_VER=1.35.0` and `QUERY_VER=1.36.0`. Override either:

```bash
WORKER_VER=1.34.0 ./run.sh     # change the workflow-run SDK version
QUERY_VER=1.35.0 ./run.sh      # change the query/replay SDK version
```

Watch the `Running workflow with SDK version: ...` and `Replaying workflow with SDK version: ...`
lines to confirm which version handled each phase.
#!/usr/bin/env bash
# Run the worker on one Temporal SDK version and query it from another.
#
#   ./run.sh                       # worker=1.35.0, query=1.36.0 (defaults)
#   WORKER_VER=1.34.0 ./run.sh     # override worker SDK version
#   QUERY_VER=1.35.0 ./run.sh      # override query-client SDK version
#
# Requires a local dev server:  temporal server start-dev
set -euo pipefail
cd "$(dirname "$0")"

WORKER_VER="${WORKER_VER:-1.35.0}"
QUERY_VER="${QUERY_VER:-1.36.0}"
WORKER_MAIN="io.temporal.samples.hello.WorkerMain"
QUERY_MAIN="io.temporal.samples.hello.QueryClient"
WORKER_LOG="target/worker.log"

if ! temporal operator cluster health >/dev/null 2>&1; then
  echo "ERROR: no local Temporal server reachable. Start one with: temporal server start-dev" >&2
  exit 1
fi

echo ">> compiling once (shared target/classes) against SDK $WORKER_VER"
mvn -q -Dtemporal.version="$WORKER_VER" compile

WORKER_PID=""
cleanup() { [[ -n "$WORKER_PID" ]] && kill "$WORKER_PID" 2>/dev/null || true; }
trap cleanup EXIT

echo ">> starting worker + starter on SDK $WORKER_VER (log: $WORKER_LOG)"
mvn -Dtemporal.version="$WORKER_VER" exec:java -Dexec.mainClass="$WORKER_MAIN" \
  >"$WORKER_LOG" 2>&1 &
WORKER_PID=$!

echo ">> waiting for workflow to complete and worker to settle..."
for _ in $(seq 1 120); do
  if grep -q "staying alive to serve queries" "$WORKER_LOG" 2>/dev/null; then break; fi
  if ! kill -0 "$WORKER_PID" 2>/dev/null; then
    echo "ERROR: worker exited early; see $WORKER_LOG" >&2; tail -20 "$WORKER_LOG" >&2; exit 1
  fi
  sleep 1
done

echo ">> querying with SDK $QUERY_VER"
mvn -q -Dtemporal.version="$QUERY_VER" exec:java -Dexec.mainClass="$QUERY_MAIN"

echo ">> done (worker left running; killed on script exit)"
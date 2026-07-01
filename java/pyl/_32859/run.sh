#!/usr/bin/env bash
# Build two self-contained jars, each pinned to one Temporal SDK version, then:
#   1. WorkerMain (WORKER_VER) runs the workflow to completion and exits.
#   2. QueryClient (QUERY_VER) starts its own worker and queries the workflow,
#      so the query is replayed on QUERY_VER -> a cross-version replay check.
#
#   ./run.sh                       # worker=1.35.0, query=1.36.0 (defaults)
#   WORKER_VER=1.34.0 ./run.sh     # override workflow-run SDK version
#   QUERY_VER=1.35.0 ./run.sh      # override query/replay SDK version
#

# Requires a local dev server:  temporal server start-dev

temporal operator search-attribute create --name MostRecentStartedActivity --type Keyword >&2


temporal operator search-attribute create --name YourAttributeName_3 --type Keyword >&2
temporal operator search-attribute create --name YourAttributeName_2 --type Keyword >&2
temporal operator search-attribute create --name YourAttributeName_1 --type Keyword >&2
temporal operator search-attribute create --name YourAttributeName --type Keyword >&2





set -euo pipefail
cd "$(dirname "$0")"

WORKER_VER="${WORKER_VER:-1.35.0}"
QUERY_VER="${QUERY_VER:-1.36.0}"
WORKER_MAIN="io.temporal.samples.hello.WorkerMain"
QUERY_MAIN="io.temporal.samples.hello.QueryClient"
WORKER_JAR="target/app-${WORKER_VER}.jar"
QUERY_JAR="target/app-${QUERY_VER}.jar"

if ! temporal operator cluster health >/dev/null 2>&1; then
  echo "ERROR: no local Temporal server reachable. Start one with: temporal server start-dev" >&2
  exit 1
fi

echo ">> building workflow-run jar (SDK $WORKER_VER) -> $WORKER_JAR"
mvn -q -Dtemporal.version="$WORKER_VER" -Dapp.finalName="app-${WORKER_VER}" clean package
if [[ "$QUERY_VER" != "$WORKER_VER" ]]; then
  echo ">> building query/replay jar (SDK $QUERY_VER) -> $QUERY_JAR"
  mvn -q -Dtemporal.version="$QUERY_VER" -Dapp.finalName="app-${QUERY_VER}" package
fi

echo ">> running workflow to completion on SDK $WORKER_VER"
java -cp "$WORKER_JAR" "$WORKER_MAIN"

echo ">> querying (starts its own worker) on SDK $QUERY_VER"
java -cp "$QUERY_JAR" "$QUERY_MAIN"

echo ">> done"
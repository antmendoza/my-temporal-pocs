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
# Connects to Temporal Cloud using src/main/resources/temporal.properties and the mTLS
# certs under src/main/resources/certs. Set temporal_local_server=true in that file to use
# a local dev server instead.


set -euo pipefail
cd "$(dirname "$0")"

WORKER_VER="${WORKER_VER:-1.35.0}"
QUERY_VER="${QUERY_VER:-1.36.0}"
WORKER_MAIN="io.temporal.samples.hello.WorkerMain"
QUERY_MAIN="io.temporal.samples.hello.QueryClient"
WORKER_JAR="target/app-${WORKER_VER}.jar"
QUERY_JAR="target/app-${QUERY_VER}.jar"

# Point the Temporal CLI at the same target as the app, read from temporal.properties.
PROPS="src/main/resources/temporal.properties"
LOCAL=$(grep -E '^temporal_local_server=' "$PROPS" | head -1 | cut -d= -f2 | tr -d '[:space:]')
if [[ "$LOCAL" != "true" ]]; then
  if [[ -z "${TEMPORAL_API_KEY:-}" ]]; then
    echo "ERROR: TEMPORAL_API_KEY must be set to connect to Temporal Cloud." >&2
    exit 1
  fi
  export TEMPORAL_ADDRESS=$(grep -E '^temporal_starter_target_endpoint=' "$PROPS" | head -1 | cut -d= -f2 | tr -d '[:space:]')
  export TEMPORAL_NAMESPACE=$(grep -E '^temporal_namespace=' "$PROPS" | head -1 | cut -d= -f2 | tr -d '[:space:]')
  export TEMPORAL_TLS=true
fi

echo ">> building workflow-run jar (SDK $WORKER_VER) -> $WORKER_JAR"
mvn -q -Dtemporal.version="$WORKER_VER" -Dapp.finalName="app-${WORKER_VER}" clean package
if [[ "$QUERY_VER" != "$WORKER_VER" ]]; then
  echo ">> building query/replay jar (SDK $QUERY_VER) -> $QUERY_JAR"
  mvn -q -Dtemporal.version="$QUERY_VER" -Dapp.finalName="app-${QUERY_VER}" package
fi

# Silence grpc-netty-shaded warnings on recent JDKs:
#   --sun-misc-unsafe-memory-access=allow  -> terminally-deprecated sun.misc.Unsafe (JDK 23+)
#   --enable-native-access=ALL-UNNAMED     -> restricted System.loadLibrary native access (JDK 22+)
JAVA_FLAGS="--sun-misc-unsafe-memory-access=allow --enable-native-access=ALL-UNNAMED"

echo ">> running workflow to completion on SDK $WORKER_VER"
java $JAVA_FLAGS -cp "$WORKER_JAR" "$WORKER_MAIN"

echo ">> querying (starts its own worker) on SDK $QUERY_VER"
java $JAVA_FLAGS -cp "$QUERY_JAR" "$QUERY_MAIN"


Kotlin 2.2.0 → 2.4.0 workflow replay non-determinism

Reproduces a Temporal replay `NonDeterministicException` caused **purely by upgrading the Kotlin
compiler** (2.2.0 → 2.4.0). The workflow source is byte-for-byte identical between the two builds; only
`kotlin.version` differs.

## Reproduce

Requires JDK 21 and Maven. Run from the `code/` directory.

#### Start temporal 

#### Run the following command
- it will start a workflow with kotlin 2.2.0 and 2.4.0 and then replay them against the other kotlin version
- Using lambda functions  (`export USE_LAMBDA=true`) to schedule the child workflows instead of method reference mitigates the issue

```bash
export USE_LAMBDA=false

# Build both, and write each module's runtime classpath to target/cp.txt
for v in 2.2.0 2.4.0; do
  ( cd "kotlin-$v" && mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt )
done
CP22="kotlin-2.2.0/target/classes:$(cat kotlin-2.2.0/target/cp.txt)"
CP24="kotlin-2.4.0/target/classes:$(cat kotlin-2.4.0/target/cp.txt)"

# 1. Record a history with each compiler
java -cp "$CP22" io.temporal.poc.sample.Recorder history-kotlin-2.2.0.json
java -cp "$CP24" io.temporal.poc.sample.Recorder history-kotlin-2.4.0.json

# 2. Controls — a history replays cleanly on the compiler that produced it
java -cp "$CP22" io.temporal.poc.sample.Replayer history-kotlin-2.2.0.json   # Replay OK
java -cp "$CP24" io.temporal.poc.sample.Replayer history-kotlin-2.4.0.json   # Replay OK

# 3. Cross-replay — NonDeterministicException both ways
# java -cp "$CP24" io.temporal.poc.sample.Replayer history-kotlin-2.2.0.json   # 2.2.0 history on 2.4.0 code
# java -cp "$CP22" io.temporal.poc.sample.Replayer history-kotlin-2.4.0.json   # 2.4.0 history on 2.2.0 code

# 4. Cross-replay with query — NonDeterministicException both ways
# start 2.2 worker
java -cp "$CP22" io.temporal.poc.sample.SampleWorker /tmp/worker22.log 2>&1 &
echo ">>>> worker kotlin 22 started" 
# the cp does not matter for the query
java -cp "$CP22" io.temporal.poc.sample.Query sample-kotlin-2.2.0
java -cp "$CP22" io.temporal.poc.sample.Query sample-kotlin-2.4.0

# kill the worker 
pkill -f io.temporal.poc.sample.SampleWorker

# start 2.4 worker
java -cp "$CP24" io.temporal.poc.sample.SampleWorker /tmp/worker24.log 2>&1 &
echo ">>>> worker kotlin 24 started" 
# the cp does not matter for the query
java -cp "$CP22" io.temporal.poc.sample.Query sample-kotlin-2.2.0
java -cp "$CP22" io.temporal.poc.sample.Query sample-kotlin-2.4.0


# kill the worker 
pkill -f io.temporal.poc.sample.SampleWorker



```

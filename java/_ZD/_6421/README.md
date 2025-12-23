## A simple demo of a hanging Temporal test

### Steps to reproduce
Run on of these three comments to reproduce the issue in the command line:
1. `./gradlew test`
2. `./gradlew test --rerun-tasks`
3. `time ./gradlew test`
#### Expected
Tests successfully finish within a few seconds (3-10 sec).
#### Actual
Tests finish after more than 30 seconds.
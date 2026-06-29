# _32859

Minimal Temporal Java SDK 1.36.0 hello-world sample.

## Run

Start a local Temporal dev server:

```bash
temporal server start-dev
```

Then run the worker + starter:

```bash
mvn compile exec:java
```

Expected output: `Hello Temporal!`

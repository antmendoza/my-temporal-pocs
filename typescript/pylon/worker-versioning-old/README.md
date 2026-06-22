# worker-versioning-old

Demonstrates the **old (Build ID-based) Worker Versioning** API in `@temporalio/*` SDK **1.15.0**.

> This API is deprecated in newer versions of the server/SDK in favor of Worker Deployment Versioning. It is kept here to reproduce/inspect 1.15-era behavior.

## Layout

- `src/worker.ts` — Worker that opts in via `buildId` + `useVersioning: true`.
- `src/workflows.ts` / `src/activities.ts` — trivial sample workflow.
- `src/client.ts` — starts a workflow on the task queue.
- `src/manage-versions.ts` — CLI to inspect and update the task queue's Build ID compatibility sets.

The task queue is `worker-versioning-old-tq`.

## Setup

```bash
npm install
```

Start a local Temporal server (e.g. `temporal server start-dev`) and ensure `TEMPORAL_ADDRESS` / `TEMPORAL_NAMESPACE` point at it (defaults: `localhost:7233` / `default`).

## Typical flow

1. Register an initial Build ID as the default set:
   ```bash
   npm run versioning -- add-new-default v1.0
   ```
2. Start a versioned worker on that Build ID:
   ```bash
   BUILD_ID=v1.0 USE_VERSIONING=true npm start
   ```
3. Run a workflow:
   ```bash
   npm run workflow
   ```
4. Inspect compatibility sets:
   ```bash
   npm run versioning -- get
   ```
5. Roll out an incompatible new default (new set):
   ```bash
   npm run versioning -- add-new-default v2.0
   BUILD_ID=v2.0 USE_VERSIONING=true npm start
   ```
6. Roll out a compatible patch (joins an existing set):
   ```bash
   npm run versioning -- add-compatible v1.1 v1.0
   ```
   Append `promote` to also promote the set to default:
   ```bash
   npm run versioning -- add-compatible v1.1 v1.0 promote
   ```
7. Promote a Build ID within its set, or a whole set:
   ```bash
   npm run versioning -- promote-within-set v1.1
   npm run versioning -- promote-set v1.0
   ```

## Notes on the SDK API

- Worker side (`@temporalio/worker`): `Worker.create({ buildId, useVersioning, ... })`.
- Client side (`@temporalio/client`): `client.taskQueue.updateBuildIdCompatibility(...)` and `client.taskQueue.getBuildIdCompatability(...)` — note the SDK 1.15 typo (`Compatability`) on the getter.

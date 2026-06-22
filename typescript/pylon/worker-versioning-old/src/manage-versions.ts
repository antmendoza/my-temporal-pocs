import { Client, Connection } from '@temporalio/client';

const TASK_QUEUE = 'worker-versioning-old-tq';

type Command =
  | { kind: 'get' }
  | { kind: 'add-new-default'; buildId: string }
  | { kind: 'add-compatible'; buildId: string; existingCompatibleBuildId: string; promoteSet?: boolean }
  | { kind: 'promote-set'; buildId: string }
  | { kind: 'promote-within-set'; buildId: string };

function parseArgs(): Command {
  const [, , cmd, a, b, c] = process.argv;
  switch (cmd) {
    case 'get':
      return { kind: 'get' };
    case 'add-new-default':
      if (!a) throw new Error('usage: add-new-default <buildId>');
      return { kind: 'add-new-default', buildId: a };
    case 'add-compatible':
      if (!a || !b) throw new Error('usage: add-compatible <newBuildId> <existingCompatibleBuildId> [promote]');
      return {
        kind: 'add-compatible',
        buildId: a,
        existingCompatibleBuildId: b,
        promoteSet: c === 'promote',
      };
    case 'promote-set':
      if (!a) throw new Error('usage: promote-set <buildIdInSet>');
      return { kind: 'promote-set', buildId: a };
    case 'promote-within-set':
      if (!a) throw new Error('usage: promote-within-set <buildId>');
      return { kind: 'promote-within-set', buildId: a };
    default:
      throw new Error(
        `unknown command "${cmd}". Available: get | add-new-default | add-compatible | promote-set | promote-within-set`,
      );
  }
}

async function main() {
  const command = parseArgs();

  const connection = await Connection.connect({
    address: process.env.TEMPORAL_ADDRESS ?? 'localhost:7233',
  });
  const client = new Client({
    connection,
    namespace: process.env.TEMPORAL_NAMESPACE ?? 'default',
  });

  switch (command.kind) {
    case 'get': {
      const sets = await client.taskQueue.getBuildIdCompatability(TASK_QUEUE);
      console.log(JSON.stringify(sets, null, 2));
      break;
    }
    case 'add-new-default': {
      await client.taskQueue.updateBuildIdCompatibility(TASK_QUEUE, {
        operation: 'addNewIdInNewDefaultSet',
        buildId: command.buildId,
      });
      console.log(`Added ${command.buildId} as new default set on ${TASK_QUEUE}`);
      break;
    }
    case 'add-compatible': {
      await client.taskQueue.updateBuildIdCompatibility(TASK_QUEUE, {
        operation: 'addNewCompatibleVersion',
        buildId: command.buildId,
        existingCompatibleBuildId: command.existingCompatibleBuildId,
        promoteSet: command.promoteSet,
      });
      console.log(
        `Added ${command.buildId} as compatible with ${command.existingCompatibleBuildId}${
          command.promoteSet ? ' (set promoted)' : ''
        }`,
      );
      break;
    }
    case 'promote-set': {
      await client.taskQueue.updateBuildIdCompatibility(TASK_QUEUE, {
        operation: 'promoteSetByBuildId',
        buildId: command.buildId,
      });
      console.log(`Promoted set containing ${command.buildId} to default`);
      break;
    }
    case 'promote-within-set': {
      await client.taskQueue.updateBuildIdCompatibility(TASK_QUEUE, {
        operation: 'promoteBuildIdWithinSet',
        buildId: command.buildId,
      });
      console.log(`Promoted ${command.buildId} to default within its set`);
      break;
    }
  }
}

main().then(
  () => process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  },
);

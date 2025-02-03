import * as workflow from '@temporalio/workflow';


import * as activities from './activities';

const nodeType = {
  AI_BLOCK: 'ai_block',
  APP: 'app',
  WAIT: 'wait',
} as const

type Node = {
  type: typeof nodeType[keyof typeof nodeType];
  children: Node[];
}

const backoffCoefficient = 2; // default value (2.0), but added for readability
const initialInterval = 1000; // default value (1s), but added for readability
const maximumAttempts = 2; // defaults to infinity
const retryPolicy: workflow.RetryPolicy = {
  backoffCoefficient,
  initialInterval,
  maximumAttempts,
  maximumInterval: initialInterval * backoffCoefficient ** maximumAttempts,
  nonRetryableErrorTypes: ['NonRetryableException', 'PrismaClientKnownRequestError'],
};

export const mainFlow = async () => {
  const { getVersionGraph, executeNodeOperation, convertToTree, getWorkspaceUser } = workflow.proxyActivities<
    typeof activities
  >({
    startToCloseTimeout: '3 Minutes',
    cancellationType: workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED,
    retry: retryPolicy,
  });

  await getVersionGraph();
  await convertToTree();



  await getWorkspaceUser();
  await executeNodeOperation(false);


  const childWorkflows: number[] = [1]

  return Promise.all(
    childWorkflows.map(async (_, itemIndex) => {
      const handle = await workflow.startChild('childWorkflow', {
        workflowId: `${workflow.workflowInfo().workflowId}-item-${itemIndex}`,
        args: [false],
      });

      return handle.result();
    }),
  );
};

export const childWorkflow = async (
  shouldFail: boolean,
) => {
  const retry = { ...retryPolicy };

  const { executeNodeOperation } = workflow.proxyActivities<typeof activities>({
    startToCloseTimeout: '3 Minutes',
    cancellationType: workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED,
    retry,
  });

  const executeAiOrAppNode = async () => {
    await executeNodeOperation(shouldFail);

    return { skipChildren: false };
  };

  const executePathNode = async () => {
    const currentWorkflowId = `${workflow.workflowInfo().workflowId}`;
    const pathSuffix = `-path-${Math.random() * 1000}`;

    const pathResults = [];

    const nodeChildren = { 1: 1 }

    for (const [index, child] of Object.entries(nodeChildren)) {

      const handle = await workflow.startChild('childWorkflow', {
        workflowId: currentWorkflowId.includes('-path-')
          ? currentWorkflowId.replace(/-path-.+/g, pathSuffix)
          : currentWorkflowId.concat(`${pathSuffix}-${index}`),
        args: [true],
      });

      pathResults.push(handle.result());
    }
    return {
      pathResults,
      skipChildren: true,
    };
  };

  const executeWaitNode = async () => {
    await executeNodeOperation(false);

    return { skipChildren: false };
  };

  const rootNode: Node = shouldFail ? { type: nodeType.AI_BLOCK, children: [] } : {
    type: nodeType.WAIT,
    children: [

      { type: nodeType.WAIT, children: [] },
      { type: nodeType.WAIT, children: [] },
      {
        type: nodeType.APP, children: [
        ]
      },
      {
        type: nodeType.APP, children: [
        ]
      },
    ]
  }

  await traverseTree(rootNode, async (node: Node) => {
    switch (node.type) {

      case nodeType.AI_BLOCK:
        return executeAiOrAppNode();

      case nodeType.APP:
        return executePathNode();

      case nodeType.WAIT:
        return executeWaitNode();
      default:
        throw new Error(`Unhandled node type ${node.type}`);
    }
  });

  return 'child done'
};


export const traverseTree = async (root: Node, processNode: (node: Node) => Promise<{ pathResults?: Promise<any>[]; skipChildren: boolean }>) => {
  const queue = [root];

  let pendingResults: Promise<any>[] = [];
  let results = [];

  while (queue.length > 0) {
    const node = queue.shift();
    if (node) {
      const result = await processNode(node);
      if (result.pathResults) {
        pendingResults = pendingResults.concat(result.pathResults);
        continue;
      }
      if (result.skipChildren) {
        results.push(result);
        continue;
      }

      if (node.children.length === 0) {
        results.push(result);
      }

      queue.push(...node.children);
    }
  }

  for (const pendingResult of pendingResults) {
    console.log('antes')
    const awaitedResult = await pendingResult;

    console.log('despues')
    results = results.concat(awaitedResult);
  }

  return results;
};
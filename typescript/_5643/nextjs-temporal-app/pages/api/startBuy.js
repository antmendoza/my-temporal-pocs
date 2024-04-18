import {Client, Connection} from '@temporalio/client';
import {OneClickBuy} from '../../temporal/lib/workflows.js';

export default async function startBuy(req, res) {

  if (req.method !== 'POST') {
    res.status(405).send({ message: 'Only POST requests allowed' });
    return;
  }

  const { itemId, transactionId } = req.body;
  if (!itemId) {
    res.status(405).send({ message: 'must send itemId to buy' });
    return;
  }

  // When the project is built, the bundler will strip out Workflow function names.
  // This ensures the workflow type remains the same
  // when the client send the request to Temporal Server. Another option is modifying the webpack config,
  // as it is stated here https://docs.temporal.io/dev-guide/typescript/debugging#production-bundling
  Object.defineProperty(OneClickBuy, "name", { value: "OneClickBuy" });

  // Connect to localhost with default ConnectionOptions,
  // pass options to the Connection constructor to configure TLS and other settings.
  const connection = await Connection.connect();
  // Workflows will be started in the "default" namespace unless specified otherwise
  // via options passed the Client constructor.
  const client = new Client({ connection });
  // kick off the purchase async
  await client.workflow.start(OneClickBuy, {
    taskQueue: 'ecommerce-oneclick',
    workflowId: transactionId,
    args: [itemId],
  });

  res.status(200).json({ ok: true });
}

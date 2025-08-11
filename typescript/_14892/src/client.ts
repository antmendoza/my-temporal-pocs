import { Metadata } from '@temporalio/client';
import { CloudOperationsClient, CloudOperationsConnection } from '@temporalio/cloud';

async function run() {
  const apiVersion = 'v0.6.1';

  const connection = await CloudOperationsConnection.connect({
    apiKey: process.env.TEMPORAL_CLOUD_API_KEY || '',
  });
  const client = new CloudOperationsClient({
    connection,
    apiVersion,
  });

  // This part will get simplified when we add high level operations to the client
  const metadata: Metadata = {
    ['temporal-cloud-api-version']: apiVersion,
  };
  const response = await client.withMetadata(metadata, async () => {
    return client.cloudService.getNamespace({
      namespace: 'antonio.a2dd6',
    });
  });
  console.log(response);



  const regions = await client.withMetadata(metadata, async () => {
    return await client.cloudService.getRegions({
    });
  });
  console.log(regions);
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});

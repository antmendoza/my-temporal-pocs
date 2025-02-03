export async function getVersionGraph() {
  console.log('getVersionGraph');
}

export async function executeNodeOperation(shouldFail: boolean) {
  console.log('executeNodeOperation');

  if (shouldFail) {
    throw new Error('Failed to execute node operation');
  }
}

export async function convertToTree() {
  console.log('convertToTree');
}

export async function getWorkspaceUser() {
  console.log('getWorkspaceUser');
}
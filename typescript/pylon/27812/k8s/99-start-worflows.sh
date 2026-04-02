#!/bin/bash


exportTEMPORAL_API_KEY

for i in {1..5}; do
  temporal workflow start \
    --address us-west-2.aws.api.temporal.io:7233 \
    --namespace "antonio.a2dd6" \
    --api-key "$TEMPORAL_API_KEY" \
    --task-queue "temporal-ts" \
    --type "example" \
    --workflow-id "$(openssl rand -hex 32)" \
    --input '{"text1":"value1"}'
done

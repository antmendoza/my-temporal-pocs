#!/usr/bin/env bash
set -euo pipefail

kubectl apply -f datadog-agent.yaml

echo "Waiting for Datadog Agent pods to be ready..."
kubectl -n temporal-metrics rollout status ds/datadog-agent

echo "Current pods:"
kubectl -n temporal-metrics get pods -o wide


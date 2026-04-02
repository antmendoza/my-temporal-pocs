#!/bin/bash

set -euo pipefail

echo "Cleaning up minikube environment..."

# Delete namespace if it exists
if kubectl get ns temporal-metrics &>/dev/null; then
    echo "Deleting temporal-metrics namespace..."
    kubectl delete ns temporal-metrics || true
fi

# Check if user wants a complete reset (delete cluster)
if [[ "${1:-}" == "--delete" ]] || [[ "${1:-}" == "-d" ]]; then
    echo "Performing complete minikube cluster deletion..."
    if minikube status &>/dev/null; then
        minikube stop || true
    fi
    minikube delete || true
    echo "Minikube cluster completely deleted. Next start will be a fresh cluster."
else
    # Just stop minikube if it's running
    if minikube status &>/dev/null; then
        echo "Stopping minikube..."
        minikube stop || true
    fi
    echo "Minikube stopped. Use '$0 --delete' for a complete cluster reset."
fi

minikube delete

echo "Cleanup complete!"
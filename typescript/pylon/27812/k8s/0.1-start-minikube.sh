#!/bin/bash

set -euo pipefail

echo "Checking minikube status..."
# Check if minikube is already running
if minikube status &>/dev/null; then
    echo "Minikube is already running. Stopping it for a clean start..."
    minikube stop || true
    sleep 2
fi

# Check if minikube cluster exists but is in a bad state
if minikube status 2>&1 | grep -q "host: Stopped\|Error"; then
    echo "Minikube cluster exists but may be in a bad state. Deleting for clean start..."
    minikube delete || true
    sleep 2
fi

echo "Starting minikube with a clean state..."
# Start minikube
# NOTE: You may see errors about 'default-storageclass' and 'storage-provisioner' addons
# during startup. These are harmless timing issues - minikube tries to enable addons
# before the API server is fully ready. The script will enable them properly after
# the API server is ready (see below).
minikube start || {
    echo "Minikube start had some issues, but continuing..."
}

echo "Waiting for Kubernetes API server to be fully ready..."
# Wait for the API server to be accessible with retries
for i in {1..30}; do
    if kubectl cluster-info &>/dev/null; then
        echo "API server is ready!"
        break
    fi
    echo "Waiting for API server... (attempt $i/30)"
    sleep 2
done

# Wait for nodes to be ready
echo "Waiting for nodes to be ready..."
kubectl wait --for=condition=Ready node --all --timeout=120s || true

# Retry enabling storage addons if they failed during startup
echo "Ensuring storage addons are enabled..."
minikube addons enable default-storageclass 2>/dev/null || echo "Note: default-storageclass addon may already be enabled"
minikube addons enable storage-provisioner 2>/dev/null || echo "Note: storage-provisioner addon may already be enabled"

echo "Verifying minikube status..."
minikube status

echo "Minikube started successfully!"

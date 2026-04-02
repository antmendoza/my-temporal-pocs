# Test the Deployment on Minikube


## 1) Start Minikube

```bash
minikube start
```

## 2) Build and make the image available to Minikube

Pick ONE of the options below.

Option A — Push to Docker Hub (recommended):

```bash
# Replace with your Docker Hub user/org
cd ..
export IMAGE=antmendoza/temporal_worker_for_ts-sdk-metrics
export TAG=v0.1.0

docker login
docker build -t ${IMAGE}:${TAG} .
docker push ${IMAGE}:${TAG}
```

Option B — Load a local image into Minikube:

```bash
cd ..
export IMAGE=temporal_worker_for_ts-sdk-metrics
export TAG=dev

docker build -t ${IMAGE}:${TAG} .
minikube image load ${IMAGE}:${TAG}
```
Note: If you use Option B, also ensure the manifests use your local tag and `imagePullPolicy: IfNotPresent`, or update images with `kubectl set image` as shown below.

## 3) Create namespace and required Secrets

```bash
kubectl apply -f namespace.yaml

# Datadog API key for the agent
kubectl -n temporal-metrics create secret generic datadog \
  --from-literal=api-key=${DD_API_KEY}

# Temporal config (TOML) used by worker and job
kubectl -n temporal-metrics create secret generic temporal-config \
  --from-file=temporal.toml=./temporal.toml
```

## 4) Deploy the Datadog Agent and Worker

```bash
kubectl apply -f datadog-agent.yaml
kubectl apply -f temporal-worker-deployment.yaml
```

If you built a custom image name or tag, update the Worker Deployment image:


Wait for pods to become Ready:

```bash
kubectl -n temporal-metrics get pods -w
```

Check logs:

```bash
kubectl -n temporal-metrics logs ds/datadog-agent --tail=100
kubectl -n temporal-metrics logs deploy/temporal-worker -f
```

## Cleanup

```bash
kubectl delete ns temporal-metrics
minikube stop
```

#!/bin/bash
cd ..
export IMAGE=antmendoza/temporal_worker_for_ts-sdk-metrics
export TAG=v0.1.0

docker login
docker build -t ${IMAGE}:${TAG} .
docker push ${IMAGE}:${TAG}

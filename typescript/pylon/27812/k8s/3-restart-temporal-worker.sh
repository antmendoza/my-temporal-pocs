#!/bin/bash


kubectl apply -f temporal-worker-deployment.yaml


kubectl -n temporal-metrics rollout restart deploy/temporal-worker

kubectl -n temporal-metrics get pods -w

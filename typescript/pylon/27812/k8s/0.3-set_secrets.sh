#!/bin/bash



kubectl -n temporal-metrics create secret generic datadog \
  --from-literal=api-key=${DD_API_KEY}


kubectl -n temporal-metrics create secret generic temporal-config \
    --from-file=temporal.toml=./temporal.toml
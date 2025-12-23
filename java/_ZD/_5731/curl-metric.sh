#!/bin/bash

curl -L -X POST 'https://{id}.live.dynatrace.com/api/v2/metrics/ingest' \
-H 'Authorization: Api-Token token' \
-H 'Content-Type: text/plain' \
--data-raw 'cpu.temperature3,dt.entity.host=HOST-06F288EE2A930951,cpu=1 55'

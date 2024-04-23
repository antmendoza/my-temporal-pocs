#!/bin/sh

go run parent-worker/main.go -target-host antonio.a2dd6.tmprl.cloud:7233 \
    -namespace antonio.a2dd6 \
    -client-cert ./cert/antonio-perez.pem -client-key ./cert/antonio-perez.key




go run child-worker/main.go -target-host antonio.a2dd6.tmprl.cloud:7233 \
    -namespace antonio.a2dd6 \
    -client-cert ./cert/antonio-perez.pem -client-key ./cert/antonio-perez.key


#!/bin/sh

go run parent-worker/main.go -target-host antonio-test-snyk.a2dd6.tmprl.cloud:7233 \
    -namespace antonio-test-snyk.a2dd6 \
    -client-cert ./cert/client.pem -client-key ./cert/client.key


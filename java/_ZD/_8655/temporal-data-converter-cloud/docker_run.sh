#!/bin/bash

docker run  -p 8888:8888 \
 -v  $PWD:/config \
 antmendoza/temporal_onboarding:latest

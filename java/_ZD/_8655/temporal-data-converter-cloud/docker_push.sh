#!/bin/bash

docker image tag antmendoza/temporal_onboarding:1.0.0 antmendoza/temporal_onboarding:latest

docker image push antmendoza/temporal_onboarding:latest
docker image push antmendoza/temporal_onboarding:1.0.0


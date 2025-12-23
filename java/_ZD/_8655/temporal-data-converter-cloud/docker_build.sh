#!/bin/bash

mvn clean install
docker build -t antmendoza/temporal_onboarding:1.0.0 .

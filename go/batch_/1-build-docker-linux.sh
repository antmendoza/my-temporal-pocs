#!/bin/sh

export DOCKER_BUILDKIT=0
export COMPOSE_DOCKER_CLI_BUILD=0
export DOCKER_DEFAULT_PLATFORM=linux/amd64


docker build -t antmendoza/snyk_worker_parent_linux -f Dockerfile_parent .


docker build -t antmendoza/snyk_worker_child_linux -f Dockerfile_parent .

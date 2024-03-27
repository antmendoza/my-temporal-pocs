#!/bin/sh

docker build -t antmendoza/snyk_worker_parent -f Dockerfile_parent .


docker build -t antmendoza/snyk_worker_child -f Dockerfile_child .

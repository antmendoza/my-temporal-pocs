#!/bin/sh


#docker-compose -f docker-compose-child.yml down --remove-orphans && docker volume prune -f

docker-compose -f docker-compose-child.yml up --scale snyk_worker_child=17 >> log_child.txt
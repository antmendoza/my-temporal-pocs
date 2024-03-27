#!/bin/sh


docker-compose -f docker-compose-parent.yml up --scale snyk_worker_parent=12 >> log_parent.txt

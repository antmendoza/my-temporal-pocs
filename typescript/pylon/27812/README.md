# Traces to DD

Before starting the collector, edit the [collector.yaml](collector%2Fcollector.yaml) file to add the dd api_key

```bash

cd collector

docker compose down --remove-orphans && docker volume prune -f

docker compose up --build

```

### Running this sample

1. `temporal server start-dev` to start [Temporal Server](https://github.com/temporalio/cli/#installation).
1. `npm install` to install dependencies.
1. `npm run start.watch` to start the Worker.
1. In another shell, `npm run workflow` to run the Workflow.

#### Metrics

Go to https://app.datadoghq.com/metric/explorer and add the following query

```
sum:temporal_request{*} by {operation}.as_rate()
```



### AWS commands

sudo yum update -y

sudo dnf install git -y

sudo yum install -y docker

sudo service docker start

sudo usermod -a -G docker ec2-user

sudo mkdir -p /usr/local/lib/docker/cli-plugins

sudo curl -L "https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64" \
-o /usr/local/lib/docker/cli-plugins/docker-compose

sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

exit




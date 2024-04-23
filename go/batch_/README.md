### Steps to run this sample:

```
docker build -t antmendoza/snyk_app_v2 .
```

```
docker-compose up --scale snyk-worker=20
```

```
docker run --rm --network="host" antmendoza/snyk_app_v2
```


```
docker-compose up --build --scale snyk-worker=20
```


docker-compose up --build --scale antmendoza/snyk_app_v2_linux=15 >> log_v2.txt


```
go run parent-worker/main.go -target-host antonio-test-snyk.a2dd6.tmprl.cloud:7233 \
    -namespace antonio-test-snyk.a2dd6 \
    -client-cert ./cert/client.pem -client-key ./cert/client.key
    
    
go run child-worker/main.go -target-host antonio.a2dd6.tmprl.cloud:7233 \
    -namespace antonio.a2dd6 \
    -client-cert ./cert/antonio-perez.pem -client-key ./cert/antonio-perez.key
    
```

```
go run ./starter_v3 -target-host antonio.a2dd6.tmprl.cloud:7233 \
    -namespace antonio.a2dd6 \
    -client-cert ./cert/antonio-perez.pem -client-key ./cert/antonio-perez.key

```

Build for linux

export DOCKER_BUILDKIT=0
export COMPOSE_DOCKER_CLI_BUILD=0
export DOCKER_DEFAULT_PLATFORM=linux/amd64


docker build -t antmendoza/snyk_app_v3_linux .

docker push antmendoza/snyk_app_v3_linux


 grep -o -i replaying log_v2_2.txt | wc -l && grep -o -i replaying log_v3.txt | wc -l





https://cloud.temporal.io/support/namespaces/antonio-test-snyk.a2dd6



## Workflow implementation

- parent workflow (go sdk)
    - start activity (go sdk)
    - start activity (ts sdk)
    - start child workflow (ts sdk)
        - start activity (ts sdk)
- end workflow (go sdk)



## Prepare environment 
### Run Jeager (WIP)

```sh
docker run --rm --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.59

```

UI in [http://localhost:16686/search](http://localhost:16686/search)



### Install dependencies


```bash
cd ctxpropagation
go mod tidy  
```

### Star temporal server


```bash
cd src_reproduction
npm install
```

## Run 

### Run the go worker, that host the workflow
```bash
cd ctxpropagation
go run worker/main.go
```



### Run the ts worker, that host the activity and child workflow
```bash
cd src_reproduction
npm run start.watch
```



### Start the workflow from go client
```bash
cd ctxpropagation
go run starter/main.go
```





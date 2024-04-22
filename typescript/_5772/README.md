# Hello World

### ts from proto

```bash

npm install 

cd src
 
protoc --plugin=./../node_modules/.bin/protoc-gen-ts_proto --ts_proto_opt=useDate=true  \
--ts_proto_out=. ./types/simple_with_timestamp.proto
```

### Running this sample
- Make sure Temporal Server is running locally (see the [quick install guide](https://docs.temporal.io/server/quick-install/)). 
- to start the Worker.

```bash
npm run start.watch
```

- In another shell,  to run the Workflow Client.

```bash
npm run workflow
```



The Workflow should return:

```bash
Hello, $date!
```

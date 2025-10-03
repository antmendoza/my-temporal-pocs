
## Start the server

```bash
temporal server start-dev
```
### Create Test namespace
```bash
temporal operator namespace create --namespace UnitTest
```


### run the test

```bash
mvn clean test
```




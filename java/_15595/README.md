
This is a sample project to demonstrate how to run a unit test that run a workflow in the temporal server
and replay it with the test framework with a different implementation.


## Start the server
```bash
temporal server start-dev
```

### run the test
```bash
mvn clean test
```


## Relevant links
- [HelloActivityReplayTest.java](src/test/java/com/antmendoza/HelloActivityReplayTest.java)




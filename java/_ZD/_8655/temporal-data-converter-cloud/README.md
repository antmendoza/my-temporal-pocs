# Codec server

This example run a workflow, with payload encrypted, and start a codec server in http://localhost:8080.

## Configuration
Configuration resides in [temporal.properties](./config/temporal.properties) file.


## Prerequisites

The code expect the namespace to have the following Search Attributes:
- CustomerId
- CustomerName
- MyCustomWid

## Start all
- client to schedule the workflow
- worker
- codec server (located in com.antmendoza.temporal.temporal.rde.httpserver)

`mvn compile exec:java -Dexec.mainClass="com.antmendoza.temporal.Main"`
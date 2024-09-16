```bash

export TEMPORAL_ENDPOINT=antonio.a2dd6.tmprl.cloud:7233
export TEMPORAL_NAMESPACE=antonio.a2dd6
export TEMPORAL_CLIENT_CERT=/Users/antmendoza/dev/temporal/certificates/namespace-antonio-perez/client.pem
export TEMPORAL_CLIENT_KEY=/Users/antmendoza/dev/temporal/certificates/namespace-antonio-perez/client.key 


./mvnw compile exec:java -Dexec.mainClass="com.antmendoza.Starter"

```
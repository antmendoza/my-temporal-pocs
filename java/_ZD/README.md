# ZD

./mvnw compile


ZD_BASE_URL=https://temporalsupport.zendesk.com ZD_EMAIL=email ZD_API_TOKEN=token  \
    ZD_VIEW_ID=4 2178291403156 \
    ./mvnw -q -DskipTests exec:java -Dexec.mainClass=io.temporal.zd.Main


# Summary of merged.json


> jq -s '.' *.json > merged.json
> 


Question: Customer is asking a question or clarification.
Problem : Customer has an issue (e.g., login failure, errors, latency) and is asking for help.
Bug: Customer is trying to do something but Temporal or the app fails / doesn’t work as expected (a clear malfunction).
Task: Customer requests an action to be done (e.g., enable Private Link, enforce SAML-only login, increase APS quota).

### Output 
6900b0a6-b4ec-832e-bdfe-6d2388ec91a1

#### Category
Count

Question  41
Problem  11
Task  2







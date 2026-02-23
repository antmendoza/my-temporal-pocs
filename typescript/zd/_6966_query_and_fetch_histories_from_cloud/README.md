# Run the example


Export env variables to connect to your namespace.


``
export TEMPORAL_ADDRESS=${namespace.accountId}.tmprl.cloud:7233
export TEMPORAL_NAMESPACE=${namespace.accountId}
export TEMPORAL_CLIENT_CERT_PATH=path_to_pem.pem
export TEMPORAL_CLIENT_KEY_PATH=path_to_key.key
``


Run
`npm run runner`
to run the client
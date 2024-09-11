# Encryption Sample

This project is a copy of https://github.com/temporalio/samples-python/tree/main/encryption 

With the following modifications:
- Create a new DefaultFailureConverter with encode_common_attributes=True

```
class FailureConverterWithDecodedAttributes(temporalio.DefaultFailureConverter):
    def __init__(self) -> None:
        super().__init__(encode_common_attributes=True)
```


- Add `failure_converter_class=FailureConverterWithDecodedAttributes` to the client.
```
    client = await Client.connect(
        "localhost:7233",
        # Use the default converter, but change the codec
        data_converter=dataclasses.replace(
            temporalio.converter.default(),
            payload_codec=EncryptionCodec(),
            failure_converter_class=FailureConverterWithDecodedAttributes,
        ),
    )
```



```bash
    poetry install
```


``` bash
poetry run python3 worker.py 

```

```bash
poetry run python3 starter.py 

```
After sending a query that does not exist in the workflow the console pring "Encoded failure"

This represents a problem since the UI send a non-existing query to the worker to get the list of queryies the workflow exposes.
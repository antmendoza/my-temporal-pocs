from temporalio.converter import DefaultFailureConverter


class FailureConverterWithDecodedAttributes(DefaultFailureConverter):
    def __init__(self) -> None:
        super().__init__(encode_common_attributes=True)

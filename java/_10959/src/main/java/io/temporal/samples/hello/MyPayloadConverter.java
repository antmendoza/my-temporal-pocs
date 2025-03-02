package io.temporal.samples.hello;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.ByteString;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.converter.DataConverterException;
import io.temporal.common.converter.EncodingKeys;
import io.temporal.common.converter.PayloadConverter;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MyPayloadConverter implements PayloadConverter {


    static final String METADATA_ENCODING_JSON_NAME = "json/plain";
    static final ByteString METADATA_ENCODING_JSON =
            ByteString.copyFrom(METADATA_ENCODING_JSON_NAME, StandardCharsets.UTF_8);

    private final ObjectMapper mapper;


    public MyPayloadConverter() {
        this(newDefaultObjectMapper());
    }

    public MyPayloadConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static ObjectMapper newDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // preserve the original value of timezone coming from the server in Payload
        // without adjusting to the host timezone
        // may be important if the replay is happening on a host in another timezone
        mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    @Override
    public String getEncodingType() {
        return METADATA_ENCODING_JSON_NAME;
    }

    @Override
    public Optional<Payload> toData(Object value) throws DataConverterException {
        try {
            byte[] serialized = mapper.writeValueAsBytes(value);
            final Optional<Payload> build = Optional.of(
                    Payload.newBuilder()
                            .putMetadata(EncodingKeys.METADATA_ENCODING_KEY, METADATA_ENCODING_JSON)
                            .setData(ByteString.copyFrom(serialized))
                            .build());


            return build;

        } catch (JsonProcessingException e) {
            throw new DataConverterException(e);
        }
    }

    @Override
    public <T> T fromData(Payload content, Class<T> valueClass, Type valueType)
            throws DataConverterException {
        ByteString data = content.getData();
        if (data.isEmpty()) {
            return null;
        }
        try {

            //The problem is that this is K or T and not a concrete class
            JavaType reference = mapper.getTypeFactory().constructType(valueType, valueClass);

            System.out.println("Reference: " + reference);

            System.out.println("valueClass: " + valueClass);

            System.out.println("valueType: " + valueType);
            final T t;
            t = mapper.readValue(content.getData().toByteArray(), reference);


            //this is added to debug the code at runtime and don't trigger the deadlock detector
            WorkflowUnsafe.deadlockDetectorOff(
                    () -> {

                        try {
                            Object a = mapper.readValue(content.getData().toByteArray(), reference);
                            System.out.println("a: " + a);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    });

            return t;

        } catch (Exception e) {
            throw new DataConverterException(e);
        }
    }
}
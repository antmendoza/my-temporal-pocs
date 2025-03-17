package com.temporal.workflow;

import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.common.converter.DataConverterException;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.lang.reflect.Type;
import java.util.Optional;

public class MyDataConverter implements io.temporal.common.converter.DataConverter {


    final int millis = 10;


    @Override
    public <T> Optional<Payload> toPayload(final T value) throws DataConverterException {
        return WorkflowUnsafe.deadlockDetectorOff(
                () -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        //throw new RuntimeException(e);
                    }
                    return Optional.empty();
                });

    }

    @Override
    public <T> T fromPayload(final Payload payload, final Class<T> valueClass, final Type valueType) throws DataConverterException {
        return WorkflowUnsafe.deadlockDetectorOff(
                () -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return null;
                });
    }

    @Override
    public Optional<Payloads> toPayloads(final Object... values) throws DataConverterException {
        return WorkflowUnsafe.deadlockDetectorOff(
                () -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return Optional.empty();
                });
    }

    @Override
    public <T> T fromPayloads(final int index, final Optional<Payloads> content, final Class<T> valueType, final Type valueGenericType) throws DataConverterException {
        return WorkflowUnsafe.deadlockDetectorOff(
                () -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return null;
                });
    }
};
package com.antmendoza;

public class AsyncChildException extends RuntimeException {
    private String id;

    public AsyncChildException(final String id, final RuntimeException failure) {
        super(failure);
        this.id = id;
    }

    public String getId() {
        return id;
    }

}

package com.antmendoza;

public class AsyncChildResult {
    private String id;
    private String childPromise;

    public AsyncChildResult(final String id, final String childPromise) {

        this.id = id;
        this.childPromise = childPromise;
    }

    public String getId() {
        return id;
    }

    public String getChildPromise() {
        return childPromise;
    }
}

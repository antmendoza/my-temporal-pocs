package com.temporal.demos.temporalspringbootdemo;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.serviceclient.SimpleSslContextBuilder;

import javax.net.ssl.SSLException;

public class SslcontextBuilderProvider {
    public SslContext getSslContext() throws SSLException {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final SslContext build = SimpleSslContextBuilder.forPKCS8(
                getClass().getResourceAsStream("/certs/client.pem"),
                getClass().getResourceAsStream("/certs/client.key")
        ).build();
        return build;


    }
}

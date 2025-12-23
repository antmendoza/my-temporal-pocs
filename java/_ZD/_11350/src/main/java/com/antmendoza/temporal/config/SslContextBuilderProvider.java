package com.antmendoza.temporal.config;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.serviceclient.SimpleSslContextBuilder;

import java.io.InputStream;

public class SslContextBuilderProvider {


    public final TemporalProperties properties;

    public SslContextBuilderProvider() {
        this.properties = new TemporalProperties();
    }

    public SslContext getSslContext() {


        if(properties.isTemporalLocalServer()){
            return null;
        }

        try {
                InputStream clientCert = getClass().getResourceAsStream(properties.getTemporalCertLocation());
            InputStream clientKey = getClass().getResourceAsStream(properties.getTemporalKeyLocation());
            return SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }



}

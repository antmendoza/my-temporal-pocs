package com.antmendoza.temporal;

import com.antmendoza.temporal.codec.CryptCodec;
import com.antmendoza.temporal.codec.EncryptedPayloadsActivity;
import com.antmendoza.temporal.temporal.rde.httpserver.RDEHttpServer;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {
        try {

            System.out.println("Start... ");

            CompletableFuture.runAsync(() -> {
                try {
                    new EncryptedPayloadsActivity().createWorkflow(CustomerIdFactory.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            new RDEHttpServer(Collections.singletonList(new CryptCodec()), 8085).start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

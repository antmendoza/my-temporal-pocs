package com.temporal.demos.temporalspringbootdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

public class RestClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        Date d1 = new Date();

        final String serviceUrl = "http://localhost:3031/start";

        HttpClient client = HttpClient.newHttpClient();



        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serviceUrl))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());




        Date d2 = new Date();

        long seconds = (d2.getTime()-d1.getTime());
        System.out.println("Request took: " + seconds);




    }
}

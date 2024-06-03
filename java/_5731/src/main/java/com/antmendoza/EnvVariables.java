package com.antmendoza;

public class EnvVariables {




    public static String getOtelEndpoint(){

        // "https://{id}.live.dynatrace.com/api/v2/metrics/ingest"
        final String value = "https://jky94838.live.dynatrace.com/api/v2/otlp";
        if(value == null){
            throw new RuntimeException("not implemented");
        }
        return value;
    }

    public static String getTracerEndpoint(){

        // "https://{id}.live.dynatrace.com/api/v2/metrics/ingest"
        final String value = null;
        if(value == null){
            throw new RuntimeException("not implemented");
        }
        return value;
    }


    public static String getDynatraceApiToken(){
        final String value = null;
        if(value == null){
            throw new RuntimeException("not implemented");
        }
        return value;
    }



}

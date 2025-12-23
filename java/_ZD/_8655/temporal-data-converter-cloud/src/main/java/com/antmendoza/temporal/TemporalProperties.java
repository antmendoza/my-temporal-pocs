package com.antmendoza.temporal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class TemporalProperties {


    public String temporal_key_location;
    public String temporal_cert_location;
    public String temporal_namespace;
    public String temporal_target_endpoint;

    public TemporalProperties() {
        this.read();
    }

    private void read() {

        FileInputStream file = null;
        Properties prop = new Properties();
        try {

            String s = "/config/temporal.properties";

            System.out.println("Loading ... " +s);

            //the base folder is ./, the root of the main.properties file
            String path = s;


            //load the file handle for main.properties
            file = new FileInputStream(new File(new File("").getAbsolutePath()+"/java/_8655/temporal-data-converter-cloud/config", "temporal.properties"));
            prop.load(file);
            loadProperties(prop);
            System.out.println("Loaded ... " +s);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                file.close();
            } catch (Exception ex) {
                //throw new RuntimeException(ex);
            }


            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("temporal.properties");
            InputStream input = resourceAsStream;
            try {

                // load a properties file
                prop.load(input);

                loadProperties(prop);

            } catch (IOException ex) {

                new RuntimeException(ex);
            } finally {
                try {
                    input.close();
                } catch (IOException ee) {
                   // throw new RuntimeException(e);
                }
            }


        }


    }

    private void loadProperties(Properties prop) {
        this.temporal_key_location = prop.getProperty("temporal_key_location");
        this.temporal_cert_location = prop.getProperty("temporal_cert_location");
        this.temporal_namespace = prop.getProperty("temporal_namespace");
        this.temporal_target_endpoint = prop.getProperty("temporal_target_endpoint");
    }
}

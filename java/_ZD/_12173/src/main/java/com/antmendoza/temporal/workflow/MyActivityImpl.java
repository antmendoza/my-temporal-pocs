package com.antmendoza.temporal.workflow;

import java.util.concurrent.CompletableFuture;

public class MyActivityImpl implements MyActivity {


    private int currentCount = 0;
    private int totalCount = 0;
    private int iteration = 0;

    //Write a function that prints an statement every 1 second for 10 seconds
    //and returns an empty string.
    public MyActivityImpl() {
        CompletableFuture.runAsync(() -> {

            while (true) {

                System.out.println("Current second: " + iteration + " , Activities per second: " + this.currentCount + ", Total activities (since started): " + this.totalCount);
                iteration ++;
                this.resetCurrentCount();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


            }


        });
    }


    @Override
    public String doSomething(String input) {

        this.processingNewActivity();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    private void processingNewActivity() {

        synchronized (this) {
            this.currentCount++;
            this.totalCount++;
        }
    }


    private void resetCurrentCount() {
        synchronized (this) {
            this.currentCount = 0;
        }
    }
}

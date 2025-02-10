package com.temporal.demos.temporalspringbootdemo;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpensiveBeanConfig {

    @Bean
    public ExpensiveService expensiveService() {
        System.out.println("Initializing ExpensiveService...");
        try {

            Thread.sleep(5_000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Initialization interrupted", e);
        }
        System.out.println("ExpensiveService initialized.");
        return new ExpensiveService();
    }
}

package com.example.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class App {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(App.class, args);
    }
}

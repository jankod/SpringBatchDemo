package com.example.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class SharedProgressLog {

    @Bean
    public List<String> progressLog() {
        return new CopyOnWriteArrayList<>();
    }
}
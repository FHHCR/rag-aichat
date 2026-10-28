package com.rag.aichat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class StreamingConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService streamingExecutor() {
        return Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "rag-stream");
            thread.setDaemon(true);
            return thread;
        });
    }
}

package com.rag.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.rag.aichat.config.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class RagAiChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagAiChatApplication.class, args);
    }
}

package com.rag.aichat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public class RagProperties {
    private final Upload upload = new Upload();
    private final Embedding embedding = new Embedding();
    private final Llm llm = new Llm();
    private final Rag rag = new Rag();

    public Upload getUpload() { return upload; }
    public Embedding getEmbedding() { return embedding; }
    public Llm getLlm() { return llm; }
    public Rag getRag() { return rag; }

    public static class Upload {
        private String dir = "./uploads";
        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }

    public static class Embedding {
        private String baseUrl;
        private String apiKey;
        private String model;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    public static class Llm {
        private String baseUrl;
        private String apiKey;
        private String model;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    public static class Rag {
        private int chunkSize = 1200;
        private int chunkOverlap = 150;
        private int topK = 5;
        private long timeoutSeconds = 40;
        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getChunkOverlap() { return chunkOverlap; }
        public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }
        public long getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(long timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }
}


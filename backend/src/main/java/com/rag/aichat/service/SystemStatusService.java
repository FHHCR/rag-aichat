package com.rag.aichat.service;

import com.rag.aichat.config.RagProperties;
import com.rag.aichat.dto.SystemStatusResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemStatusService {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final RagProperties ragProperties;

    public SystemStatusService(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate, RagProperties ragProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.ragProperties = ragProperties;
    }

    public SystemStatusResponse status() {
        SystemStatusResponse response = new SystemStatusResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setAppVersion("v1.0");
        response.setComponents(List.of(
                dbStatus(),
                pgvectorStatus(),
                redisStatus(),
                uploadStatus(),
                status("Embedding", configured(ragProperties.getEmbedding().getBaseUrl()) && configured(ragProperties.getEmbedding().getApiKey()), ragProperties.getEmbedding().getModel()),
                status("LLM", configured(ragProperties.getLlm().getBaseUrl()) && configured(ragProperties.getLlm().getApiKey()), ragProperties.getLlm().getModel()),
                status("Tika", true, "Apache Tika")
        ));
        return response;
    }

    private SystemStatusResponse.ComponentStatus dbStatus() {
        try {
            jdbcTemplate.queryForObject("select 1", Integer.class);
            return status("Database", true, "reachable");
        } catch (Exception e) {
            return status("Database", false, e.getMessage());
        }
    }

    private SystemStatusResponse.ComponentStatus redisStatus() {
        try {
            redisTemplate.opsForValue().get("rag-aichat:status");
            return status("Redis", true, "reachable");
        } catch (Exception e) {
            return status("Redis", false, e.getMessage());
        }
    }

    private SystemStatusResponse.ComponentStatus pgvectorStatus() {
        try {
            Integer count = jdbcTemplate.queryForObject("select count(*) from pg_extension where extname = 'vector'", Integer.class);
            boolean enabled = count != null && count > 0;
            return status("pgvector", enabled, enabled ? "extension enabled" : "extension not found");
        } catch (DataAccessException e) {
            return status("pgvector", false, e.getMessage());
        }
    }

    private SystemStatusResponse.ComponentStatus uploadStatus() {
        try {
            Path path = Paths.get(ragProperties.getUpload().getDir());
            Files.createDirectories(path);
            Path probe = Files.createTempFile(path, "rag-status", ".tmp");
            Files.delete(probe);
            return status("Upload directory", true, path.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private SystemStatusResponse.ComponentStatus status(String name, boolean healthy, String detail) {
        return new SystemStatusResponse.ComponentStatus(name, healthy ? "UP" : "DOWN", detail);
    }

    private boolean configured(String value) {
        return value != null && !value.isBlank();
    }
}


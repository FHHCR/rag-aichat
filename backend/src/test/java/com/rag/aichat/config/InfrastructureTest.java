package com.rag.aichat.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Tag("infrastructure")
class InfrastructureTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void postgresAndRedisAreAvailable() {
        assertNotNull(jdbcTemplate);
        assertNotNull(redisTemplate);
        assertEquals(1, jdbcTemplate.queryForObject("select 1", Integer.class));
        redisTemplate.opsForValue().set("rag-aichat:ping", "pong");
        assertEquals("pong", redisTemplate.opsForValue().get("rag-aichat:ping"));
    }
}

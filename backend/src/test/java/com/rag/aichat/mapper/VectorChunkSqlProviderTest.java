package com.rag.aichat.mapper;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorChunkSqlProviderTest {

    @Test
    void searchSqlBindsAllParameters() {
        VectorChunkSqlProvider provider = new VectorChunkSqlProvider();
        Map<String, Object> params = Map.of(
                "vectorLiteral", "[0.1,0.2]",
                "knowledgeBaseIds", List.of("kb-a", "kb-b", "kb-c"),
                "topK", 5
        );

        String sql = provider.search(params);
        assertTrue(sql.contains("knowledge_base_id in (#{knowledgeBaseIds[0]},#{knowledgeBaseIds[1]},#{knowledgeBaseIds[2]})"));
        assertTrue(sql.contains("cast(#{vectorLiteral} as vector)"));
        assertTrue(sql.contains("limit #{topK}"));

        Configuration configuration = new Configuration();
        SqlSourceBuilder builder = new SqlSourceBuilder(configuration);
        BoundSql boundSql = builder.parse(sql, Map.class, null).getBoundSql(params);
        MetaObject metaObject = SystemMetaObject.forObject(params);
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            assertNotNull(metaObject.getValue(mapping.getProperty()));
        }
    }
}

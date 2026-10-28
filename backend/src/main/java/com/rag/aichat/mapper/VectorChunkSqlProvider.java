package com.rag.aichat.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VectorChunkSqlProvider {

    public String search(Map<String, Object> context) {
        List<String> knowledgeBaseIds = (List<String>) context.get("knowledgeBaseIds");
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            throw new IllegalArgumentException("knowledgeBaseIds must not be empty");
        }
        String inClause = IntStream.range(0, knowledgeBaseIds.size())
                .mapToObj(index -> "#{knowledgeBaseIds[" + index + "]}")
                .collect(Collectors.joining(",", "(", ")"));
        return """
                select c.id, c.document_file_id, c.knowledge_base_id, c.chunk_index, c.content, c.created_at
                from document_chunks c
                where c.knowledge_base_id in %s
                order by c.embedding <=> cast(#{vectorLiteral} as vector)
                limit #{topK}
                """.formatted(inClause);
    }
}

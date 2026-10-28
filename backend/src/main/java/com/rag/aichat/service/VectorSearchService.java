package com.rag.aichat.service;

import com.rag.aichat.dto.DocumentChunkView;
import com.rag.aichat.mapper.VectorChunkMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorSearchService {

    private final VectorChunkMapper vectorChunkMapper;

    public VectorSearchService(VectorChunkMapper vectorChunkMapper) {
        this.vectorChunkMapper = vectorChunkMapper;
    }

    public void insert(String id, String documentFileId, String knowledgeBaseId, int chunkIndex, String content, String embeddingProvider, String embeddingModel, float[] embedding) {
        vectorChunkMapper.insertChunk(
                id,
                documentFileId,
                knowledgeBaseId,
                chunkIndex,
                content,
                embeddingProvider,
                embeddingModel,
                vectorLiteral(embedding)
        );
    }

    public List<DocumentChunkView> search(float[] vector, List<String> knowledgeBaseIds, int topK) {
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return List.of();
        }
        return vectorChunkMapper.search(vectorLiteral(vector), knowledgeBaseIds, topK);
    }

    public String vectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }
}


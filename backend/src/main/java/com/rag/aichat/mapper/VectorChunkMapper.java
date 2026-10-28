package com.rag.aichat.mapper;

import com.rag.aichat.dto.DocumentChunkView;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface VectorChunkMapper {

    @Insert("""
            insert into document_chunks(id, document_file_id, knowledge_base_id, chunk_index, content, embedding_provider, embedding_model, embedding, created_at)
            values (#{id}, #{documentFileId}, #{knowledgeBaseId}, #{chunkIndex}, #{content}, #{embeddingProvider}, #{embeddingModel}, cast(#{embeddingLiteral} as vector), now())
            """)
    int insertChunk(@Param("id") String id,
                    @Param("documentFileId") String documentFileId,
                    @Param("knowledgeBaseId") String knowledgeBaseId,
                    @Param("chunkIndex") int chunkIndex,
                    @Param("content") String content,
                    @Param("embeddingProvider") String embeddingProvider,
                    @Param("embeddingModel") String embeddingModel,
                    @Param("embeddingLiteral") String embeddingLiteral);

    @SelectProvider(type = VectorChunkSqlProvider.class, method = "search")
    List<DocumentChunkView> search(@Param("vectorLiteral") String vectorLiteral,
                                   @Param("knowledgeBaseIds") List<String> knowledgeBaseIds,
                                   @Param("topK") int topK);

    @Delete("delete from document_chunks where document_file_id = #{documentFileId}")
    int deleteByDocumentFileId(@Param("documentFileId") String documentFileId);
}

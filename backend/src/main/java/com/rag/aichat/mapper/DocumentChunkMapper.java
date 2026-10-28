package com.rag.aichat.mapper;

import com.rag.aichat.dto.DocumentChunkView;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DocumentChunkMapper {

    @Select("select count(*) from document_chunks where document_file_id = #{documentFileId}")
    long countByDocumentFileId(String documentFileId);

    @Delete("delete from document_chunks where document_file_id = #{documentFileId}")
    int deleteByDocumentFileId(String documentFileId);

    @Select("""
            select c.id, c.document_file_id, c.knowledge_base_id, c.chunk_index, c.content, c.created_at
            from document_chunks c
            where c.document_file_id = #{documentFileId}
            order by c.chunk_index
            """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = String.class),
            @Arg(column = "document_file_id", javaType = String.class),
            @Arg(column = "knowledge_base_id", javaType = String.class),
            @Arg(column = "chunk_index", javaType = int.class),
            @Arg(column = "content", javaType = String.class),
            @Arg(column = "created_at", javaType = LocalDateTime.class)
    })
    List<DocumentChunkView> findByDocumentFileId(String documentFileId);
}

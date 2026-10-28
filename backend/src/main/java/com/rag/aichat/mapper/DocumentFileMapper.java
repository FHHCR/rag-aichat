package com.rag.aichat.mapper;

import com.rag.aichat.entity.DocumentFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DocumentFileMapper {

    @Insert("""
            insert into document_files(id, knowledge_base_id, file_name, original_path, mime_type, file_size,
                parser, status, parse_error, chunk_count, uploaded_by_id, created_at, updated_at)
            values (#{id}, #{knowledgeBaseId}, #{fileName}, #{originalPath}, #{mimeType}, #{fileSize},
                #{parser}, #{status}, #{parseError}, #{chunkCount}, #{uploadedById}, now(), now())
            """)
    int insert(DocumentFile file);

    @Update("""
            update document_files
            set status = #{status},
                parse_error = #{parseError},
                chunk_count = #{chunkCount},
                updated_at = now()
            where id = #{id}
            """)
    int update(DocumentFile file);

    @Select("select * from document_files where id = #{id}")
    DocumentFile findById(String id);

    @Select("select * from document_files where knowledge_base_id = #{knowledgeBaseId} order by created_at desc")
    List<DocumentFile> findByKnowledgeBaseIdOrderByCreatedAtDesc(String knowledgeBaseId);

    @Delete("delete from document_files where knowledge_base_id = #{knowledgeBaseId}")
    int deleteByKnowledgeBaseId(String knowledgeBaseId);

    @Delete("delete from document_files where id = #{id}")
    int deleteById(String id);
}

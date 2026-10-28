package com.rag.aichat.mapper;

import com.rag.aichat.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {

    @Insert("""
            insert into knowledge_bases(id, name, description, status, created_by_id, created_at, updated_at)
            values (#{id}, #{name}, #{description}, #{status}, #{createdById}, now(), now())
            """)
    int insert(KnowledgeBase knowledgeBase);

    @Update("""
            update knowledge_bases
            set name = #{name},
                description = #{description},
                updated_at = now()
            where id = #{id}
            """)
    int update(KnowledgeBase knowledgeBase);

    @Select("select * from knowledge_bases where id = #{id}")
    KnowledgeBase findById(String id);

    @Select("select count(*) from knowledge_bases where id = #{id}")
    long countById(String id);

    @Delete("delete from knowledge_bases where id = #{id}")
    int deleteById(String id);

    @Select("select * from knowledge_bases order by created_at desc")
    List<KnowledgeBase> findAllByOrderByCreatedAtDesc();

    @Select("select * from knowledge_bases where status = #{status} order by created_at desc")
    List<KnowledgeBase> findAllByStatusOrderByCreatedAtDesc(String status);
}

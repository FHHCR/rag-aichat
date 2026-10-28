package com.rag.aichat.mapper;

import com.rag.aichat.entity.ChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    @Insert("""
            insert into chat_sessions(id, user_id, title, knowledge_base_scope, is_deleted, deleted_at, created_at, updated_at)
            values (#{id}, #{userId}, #{title}, cast(#{knowledgeBaseScope} as jsonb), #{deleted}, #{deletedAt}, now(), now())
            """)
    int insert(ChatSession session);

    @Update("""
            update chat_sessions
            set title = #{title},
                knowledge_base_scope = cast(#{knowledgeBaseScope} as jsonb),
                is_deleted = #{deleted},
                deleted_at = #{deletedAt},
                updated_at = now()
            where id = #{id}
            """)
    int update(ChatSession session);

    @Select("select * from chat_sessions where id = #{id}")
    ChatSession findById(String id);

    @Select("""
            select * from chat_sessions
            where user_id = #{userId} and is_deleted = false
            order by updated_at desc
            """)
    List<ChatSession> findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(String userId);

    @Select("""
            select * from chat_sessions
            where id = #{id} and user_id = #{userId} and is_deleted = false
            """)
    ChatSession findByIdAndUserIdAndDeletedFalse(@Param("id") String id, @Param("userId") String userId);
}

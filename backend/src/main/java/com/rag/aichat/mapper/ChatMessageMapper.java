package com.rag.aichat.mapper;

import com.rag.aichat.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Insert("""
            insert into chat_messages(id, session_id, user_id, role, content, source_json, created_at)
            values (#{id}, #{sessionId}, #{userId}, #{role}, #{content}, cast(#{sourceJson} as jsonb), now())
            """)
    int insert(ChatMessage message);

    @Select("select * from chat_messages where id = #{id}")
    ChatMessage findById(String id);

    @Select("""
            select * from chat_messages
            where session_id = #{sessionId} and user_id = #{userId}
            order by created_at asc
            """)
    List<ChatMessage> findBySessionIdAndUserIdOrderByCreatedAtAsc(@Param("sessionId") String sessionId,
                                                                  @Param("userId") String userId);
}

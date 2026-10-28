package com.rag.aichat.mapper;

import com.rag.aichat.entity.User;
import com.rag.aichat.entity.UserRole;
import com.rag.aichat.entity.UserStatus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("""
            insert into users(id, username, password_hash, display_name, role, status, created_at, updated_at)
            values (#{id}, #{username}, #{passwordHash}, #{displayName}, #{role}, #{status}, now(), now())
            """)
    int insert(User user);

    @Update("""
            update users
            set display_name = #{displayName},
                password_hash = #{passwordHash},
                role = #{role},
                status = #{status},
                updated_at = now()
            where id = #{id}
            """)
    int update(User user);

    @Select("select * from users where id = #{id}")
    User findById(String id);

    @Select("select * from users order by created_at desc")
    List<User> findAll();

    @Select("select * from users where username = #{username}")
    User findByUsername(String username);

    @Select("select count(*) from users where username = #{username}")
    long countByUsername(String username);

    @Select("select count(*) from users where role = #{role} and status = #{status}")
    long countByRoleAndStatus(UserRole role, UserStatus status);

    @Delete("delete from users")
    int deleteAll();
}

package com.example.staffassign.repository;

import com.example.staffassign.entity.UserMaster;
import org.apache.ibatis.annotations.*;

import java.util.Optional;
import java.util.List;

@Mapper
public interface UserMasterMapper {
    @Select("SELECT * FROM user_master WHERE email = #{email}")
    Optional<UserMaster> findByEmail(String email);
    
    @Select("SELECT * FROM user_master ORDER BY email")
    List<UserMaster> findAll();
    
    @Insert("INSERT INTO user_master (email, valid_from, valid_to, role, password_hash, created_at) " +
            "VALUES (#{email}, #{validFrom}, #{validTo}, #{role}, #{passwordHash}, CURRENT_TIMESTAMP)")
    void insert(UserMaster user);
    
    @Update("UPDATE user_master SET valid_from=#{validFrom}, valid_to=#{validTo}, role=#{role}, password_hash=#{passwordHash} WHERE email=#{email}")
    void update(UserMaster user);
    
    @Delete("DELETE FROM user_master WHERE email = #{email}")
    void delete(String email);
}

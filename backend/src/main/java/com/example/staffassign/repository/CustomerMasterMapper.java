package com.example.staffassign.repository;

import com.example.staffassign.entity.CustomerMaster;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CustomerMasterMapper {
    @Select("SELECT * FROM customer_master ORDER BY id")
    List<CustomerMaster> findAll();

    @Select("SELECT * FROM customer_master WHERE id = #{id}")
    Optional<CustomerMaster> findById(Long id);

    @Insert("INSERT INTO customer_master (name, contact_person, email, phone, created_at, updated_at) " +
            "VALUES (#{name}, #{contactPerson}, #{email}, #{phone}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CustomerMaster customer);

    @Update("UPDATE customer_master SET name=#{name}, contact_person=#{contactPerson}, email=#{email}, " +
            "phone=#{phone}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    void update(CustomerMaster customer);

    @Delete("DELETE FROM customer_master WHERE id = #{id}")
    void delete(Long id);
}

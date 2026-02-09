package com.example.staffassign.repository;

import com.example.staffassign.entity.ProjectMaster;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMasterMapper {
    @Select("SELECT * FROM project_master ORDER BY id")
    List<ProjectMaster> findAll();

    @Select("SELECT * FROM project_master WHERE id = #{id}")
    Optional<ProjectMaster> findById(Long id);

    @Insert("INSERT INTO project_master (name, description, customer_id, end_user, start_ym, end_ym, parent_id, revenue, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{customerId}, #{endUser}, #{startYm}, #{endYm}, #{parentId}, #{revenue}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectMaster project);

    @Update("UPDATE project_master SET name=#{name}, description=#{description}, customer_id=#{customerId}, " +
            "end_user=#{endUser}, start_ym=#{startYm}, end_ym=#{endYm}, parent_id=#{parentId}, revenue=#{revenue}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    void update(ProjectMaster project);

    @Delete("DELETE FROM project_master WHERE id = #{id}")
    void delete(Long id);
}

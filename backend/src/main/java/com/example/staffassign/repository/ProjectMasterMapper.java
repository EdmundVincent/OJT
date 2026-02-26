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

    @Insert("INSERT INTO project_master (name, description, customer_id, end_user, start_ym, end_ym, parent_id, revenue, pm_id, priority, project_type, status, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{customerId}, #{endUser}, #{startYm}, #{endYm}, #{parentId}, #{revenue}, #{pmId}, #{priority}, #{projectType}, #{status}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectMaster project);

    @Update("UPDATE project_master SET name=#{name}, description=#{description}, customer_id=#{customerId}, " +
            "end_user=#{endUser}, start_ym=#{startYm}, end_ym=#{endYm}, parent_id=#{parentId}, revenue=#{revenue}, " +
            "pm_id=#{pmId}, priority=#{priority}, project_type=#{projectType}, status=#{status}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    void update(ProjectMaster project);

    @Delete("DELETE FROM project_master WHERE id = #{id}")
    void delete(Long id);
}

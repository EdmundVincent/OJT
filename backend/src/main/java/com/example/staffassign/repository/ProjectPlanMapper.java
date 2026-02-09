package com.example.staffassign.repository;

import com.example.staffassign.entity.ProjectPlan;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectPlanMapper {
    @Select("SELECT * FROM project_plan WHERE project_id = #{projectId} ORDER BY plan_version DESC")
    List<ProjectPlan> findByProjectId(Long projectId);

    @Select("SELECT * FROM project_plan WHERE id = #{id}")
    Optional<ProjectPlan> findById(Long id);

    @Select("SELECT MAX(end_ym) FROM project_plan WHERE project_id = #{projectId}")
    Integer findMaxEndYmByProjectId(Long projectId);

    @Insert("INSERT INTO project_plan (project_id, plan_version, start_ym, end_ym, resource_count, production_amount, created_at) " +
            "VALUES (#{projectId}, #{planVersion}, #{startYm}, #{endYm}, #{resourceCount}, #{productionAmount}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectPlan plan);
    
    @Select("SELECT * FROM project_plan ORDER BY project_id, plan_version")
    List<ProjectPlan> findAll();

    @Update("UPDATE project_plan SET project_id = #{projectId}, plan_version = #{planVersion}, start_ym = #{startYm}, end_ym = #{endYm}, " +
            "resource_count = #{resourceCount}, production_amount = #{productionAmount} WHERE id = #{id}")
    void update(ProjectPlan plan);

    @Delete("DELETE FROM project_plan WHERE id = #{id}")
    void delete(Long id);
}

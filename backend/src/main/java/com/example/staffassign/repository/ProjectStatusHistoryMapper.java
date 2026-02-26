package com.example.staffassign.repository;

import com.example.staffassign.entity.ProjectStatusHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectStatusHistoryMapper {
    @Insert("INSERT INTO project_status_history (project_id, old_status, new_status, reason, changed_by, changed_at) " +
            "VALUES (#{projectId}, #{oldStatus}, #{newStatus}, #{reason}, #{changedBy}, #{changedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectStatusHistory history);

    @Select("SELECT * FROM project_status_history WHERE project_id = #{projectId} ORDER BY changed_at DESC")
    List<ProjectStatusHistory> findByProjectId(Long projectId);
}


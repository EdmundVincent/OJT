package com.example.staffassign.repository;

import com.example.staffassign.entity.Assignment;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AssignmentMapper {
    @Select("SELECT * FROM assignment ORDER BY id")
    List<Assignment> findAll();

    @Select("SELECT * FROM assignment WHERE project_id = #{projectId}")
    List<Assignment> findByProjectId(Long projectId);
    
    @Select("SELECT * FROM assignment WHERE employee_id = #{employeeId}")
    List<Assignment> findByEmployeeId(Long employeeId);

    @Select("SELECT * FROM assignment WHERE id = #{id}")
    Optional<Assignment> findById(Long id);
    
    @Select("SELECT MAX(end_ym) FROM assignment WHERE project_id = #{projectId} AND employee_id = #{employeeId}")
    Integer findMaxEndYm(Long projectId, Long employeeId);

    @Insert("INSERT INTO assignment (project_id, employee_id, start_ym, end_ym, unit_price, allocation_ratio, created_at, updated_at) " +
            "VALUES (#{projectId}, #{employeeId}, #{startYm}, #{endYm}, #{unitPrice}, #{allocationRatio}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Assignment assignment);

    @Update("UPDATE assignment SET start_ym=#{startYm}, end_ym=#{endYm}, unit_price=#{unitPrice}, " +
            "allocation_ratio=#{allocationRatio}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    void update(Assignment assignment);

    @Delete("DELETE FROM assignment WHERE id = #{id}")
    void delete(Long id);
}

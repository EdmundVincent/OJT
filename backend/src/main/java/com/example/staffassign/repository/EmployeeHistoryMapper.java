package com.example.staffassign.repository;

import com.example.staffassign.dto.EmployeeHistoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeHistoryMapper {
    @Select("""
            SELECT 
              a.id AS assignment_id,
              p.id AS project_id,
              p.name AS project_name,
              p.status AS project_status,
              a.start_ym AS start_ym,
              a.end_ym AS end_ym
            FROM assignment a
            JOIN project_master p ON a.project_id = p.id
            WHERE a.employee_id = #{employeeId}
            ORDER BY a.start_ym
            """)
    List<EmployeeHistoryItem> findByEmployeeId(@Param("employeeId") Long employeeId);
}


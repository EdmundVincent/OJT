package com.example.staffassign.repository;

import com.example.staffassign.entity.EmployeeMaster;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeMasterMapper {
    @Select("SELECT * FROM employee_master ORDER BY id")
    List<EmployeeMaster> findAll();

    @Select("SELECT * FROM employee_master WHERE id = #{id}")
    Optional<EmployeeMaster> findById(Long id);

    @Select("SELECT * FROM employee_master WHERE employee_code = #{employeeCode}")
    Optional<EmployeeMaster> findByEmployeeCode(String employeeCode);

    @Insert("INSERT INTO employee_master (employee_code, name, join_year, rank, department, skills, remarks, created_at, updated_at) " +
            "VALUES (#{employeeCode}, #{name}, #{joinYear}, #{rank}, #{department}, #{skills}, #{remarks}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(EmployeeMaster employee);

    @Update("UPDATE employee_master SET employee_code=#{employeeCode}, name=#{name}, join_year=#{joinYear}, rank=#{rank}, " +
            "department=#{department}, skills=#{skills}, remarks=#{remarks}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    void update(EmployeeMaster employee);

    @Delete("DELETE FROM employee_master WHERE id = #{id}")
    void delete(Long id);
}

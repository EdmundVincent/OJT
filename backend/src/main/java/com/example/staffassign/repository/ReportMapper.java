package com.example.staffassign.repository;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    List<Map<String, Object>> getResourceAllocation(Map<String, Object> params);
    List<Map<String, Object>> getBudgetMismatch(Map<String, Object> params);
    List<Map<String, Object>> getProjectCost(Map<String, Object> params);
}

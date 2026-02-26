package com.example.staffassign.repository;

import com.example.staffassign.entity.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface AuditLogMapper {
    @Insert("INSERT INTO audit_log (action_type, target_type, target_id, detail, performed_by, performed_at) " +
            "VALUES (#{actionType}, #{targetType}, #{targetId}, #{detail}, #{performedBy}, #{performedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AuditLog log);
}


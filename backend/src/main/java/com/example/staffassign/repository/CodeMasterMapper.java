package com.example.staffassign.repository;

import com.example.staffassign.entity.CodeMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CodeMasterMapper {
    @Select("SELECT * FROM code_master ORDER BY category_id, sort_order")
    List<CodeMaster> findAll();

    @Select("SELECT * FROM code_master WHERE category_id = #{categoryId} ORDER BY sort_order")
    List<CodeMaster> findByCategory(String categoryId);

    @Select("SELECT * FROM code_master WHERE category_id = #{categoryId} AND code_id = #{codeId}")
    CodeMaster findByKey(String categoryId, String codeId);

    @org.apache.ibatis.annotations.Insert("INSERT INTO code_master (category_id, category_name, code_id, code_name, sort_order, created_at, updated_at) " +
            "VALUES (#{categoryId}, #{categoryName}, #{codeId}, #{codeName}, #{sortOrder}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    void insert(CodeMaster codeMaster);

    @org.apache.ibatis.annotations.Update("UPDATE code_master SET category_name = #{categoryName}, code_name = #{codeName}, " +
            "sort_order = #{sortOrder}, updated_at = CURRENT_TIMESTAMP WHERE category_id = #{categoryId} AND code_id = #{codeId}")
    void update(CodeMaster codeMaster);
}

package com.hr.dto;
import lombok.Data;
@Data
public class HrDepartmentQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String deptName;   // 模糊查询
    private Integer status;    // 1 启用 0 停用
}


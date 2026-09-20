package com.hr.entity;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class HrDepartment implements Serializable {
    private Long id;
    private String deptName;
    private Long parentId;     // 上级部门，0 表示顶级
    private String deptCode;
    private Long leaderId;     // 负责人（关联 hr_employee.id）
    private Integer sort;
    private Integer status;    // 1 启用 0 停用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
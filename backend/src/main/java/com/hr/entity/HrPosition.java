package com.hr.entity;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class HrPosition implements Serializable {
    private Long id;
    private String postName;
    private String postCode;
    private Long deptId;
    private Integer headcount;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
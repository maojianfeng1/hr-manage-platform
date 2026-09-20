package com.hr.dto;
import lombok.Data;
@Data
public class HrPositionQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String postName;
    private Long deptId;
    private Integer status;
}

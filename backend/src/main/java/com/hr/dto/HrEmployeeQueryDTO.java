package com.hr.dto;
import lombok.Data;
@Data
public class HrEmployeeQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String empCode;
    private String name;
    private Long deptId;
    private Integer status;
    /** 员工本人 id：不暴露给前端，由 Service 在"员工只看自己"时强制塞值 */
    private Long employeeId;
}


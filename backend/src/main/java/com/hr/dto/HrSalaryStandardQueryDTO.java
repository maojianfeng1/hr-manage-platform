package com.hr.dto;
import lombok.Data;
@Data
public class HrSalaryStandardQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private Long employeeId;
}

package com.hr.entity;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class HrSalaryStandard implements Serializable {
    private Long id;
    private Long employeeId;
    private BigDecimal baseSalary;
    private BigDecimal postSalary;
    private BigDecimal perfSalary;
    private BigDecimal socialBase;
    private BigDecimal fundBase;
    private String bankCard;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}


package com.hr.entity;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class HrPayroll implements Serializable {
    private Long id;
    private Long employeeId;
    private String salaryMonth;
    private String empName;
    private String deptName;
    private String postName;
    private BigDecimal baseSalary;
    private BigDecimal postSalary;
    private BigDecimal perfSalary;
    private BigDecimal overtimePay;
    private BigDecimal attendanceDeduct;
    private BigDecimal grossPay;
    private BigDecimal socialPersonal;
    private BigDecimal fundPersonal;
    private BigDecimal tax;
    private BigDecimal netPay;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}


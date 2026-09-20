package com.hr.entity;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
@Data
public class HrAttendance implements Serializable {
    private Long id;
    private Long employeeId;
    private String empName;     // 姓名快照
    private Long deptId;
    private LocalDate attendDate;
    private LocalTime clockIn;
    private LocalTime clockOut;
    private String status;
    private Integer leaveType;
    private BigDecimal overtimeHours;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

package com.hr.vo;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class AttendanceSummaryVO {
    private BigDecimal overtimeHours;   // 加班总小时
    private Integer lateCount;          // 迟到次数
    private Integer earlyCount;         // 早退次数
    private Integer absentCount;        // 旷工次数
    private Integer shijiaCount;        // 事假天数
    private Integer bingjiaCount;       // 病假天数
}


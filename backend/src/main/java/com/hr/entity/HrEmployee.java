package com.hr.entity;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class HrEmployee implements Serializable {
    private Long id;
    private String empCode;     // 工号
    private String name;
    private Integer gender;     // 1 男 2 女
    private String phone;
    private String email;
    private String idCard;
    private Long deptId;
    private Long postId;
    private String jobLevel;
    private LocalDate entryDate;
    private LocalDate regularDate;
    private Integer status;     // 1 试用 2 在职 3 离职
    private String education;
    private String avatar;
    private String bankCard;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}


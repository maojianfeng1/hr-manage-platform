package com.hr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求参数（入参 DTO）
 *
 * 知识点：为什么收参数要用 DTO 而不是 Map 或实体类？
 * 1. DTO（Data Transfer Object）=「专门用来在层与层之间传数据」的对象，
 *    它的字段与「这一次的请求」一一对应，和数据库表结构无关。
 * 2. @NotBlank 配合 Controller 上的 @Valid，由 spring-boot-starter-validation 自动校验，
 *    不合法时抛 MethodArgumentNotValidException，被全局异常处理器捕获成 PARAM_ERROR。
 *    这样业务代码里就完全不用写 if (username == null) 这种样板判断。
 */
@Data
public class LoginDTO implements Serializable {

    /** 登录账号 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（明文，仅用于本次校验，绝不持久化、绝不回传） */
    @NotBlank(message = "密码不能为空")
    private String password;
}

package com.hr.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
  方法级权限注解：贴在 Controller 方法上，表示「访问该方法需要拥有指定权限标识」。
 示例：@RequiresPermission("emp:edit")  public Result<?> update(...) {}
 拦截器会在请求到达方法前，校验当前登录用户的 perms 集合是否包含 "emp:edit"，
 不包含直接返回 403，方法体内代码根本不执行。
 对比「在方法里 if(!hasPerm) throw...」：注解式把权限判断从业务代码抽离，更声明式、更干净，
  思想和 Spring Security 的 @PreAuthorize 一致。
 */
@Target(ElementType.METHOD)              // 只能贴在方法上
@Retention(RetentionPolicy.RUNTIME)     // 运行时保留，拦截器才能通过反射读到
public @interface RequiresPermission {
    String value();                      // 权限标识，如 "emp:edit"
}

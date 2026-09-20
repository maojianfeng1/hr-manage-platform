package com.hr.common.result;

import lombok.Getter;

/**
 * 业务状态码枚举
 *
 * 知识点：为什么用枚举而不是散落的魔法数字？
 * 1. code 与 msg 成对定义，改文案只改一处；
 * 2. 编译期检查，写错枚举名 IDE 直接标红，不会出现「500 写成 50」这种低级 bug；
 * 3. 前端可以与后端约定同一套 code，401 跳登录、403 提示无权限，这是前后端协作的契约。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录状态已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    ERROR(500, "系统内部异常"),
    BUSINESS_ERROR(600, "业务处理失败");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

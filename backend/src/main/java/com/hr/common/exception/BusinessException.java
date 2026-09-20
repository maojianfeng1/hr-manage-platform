package com.hr.common.exception;

import com.hr.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：由业务代码主动抛出，如「员工已存在」「无权限操作」
 *
 * 知识点：为什么不直接用 RuntimeException？
 * 全局异常处理器（GlobalExceptionHandler）要能区分「业务异常」和「系统崩溃」。
 * BusinessException 带一个 code，处理器能据此返回 600（或自定义）给前端，
 * 而不是统统返回 500。
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}

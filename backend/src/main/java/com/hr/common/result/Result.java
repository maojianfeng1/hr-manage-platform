package com.hr.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * 知识点：为什么必须统一？
 * 前端只认一种格式：{ code, msg, data }。
 * 如果不同接口返回结构不一样（有的返回数组、有的返回对象、有的报错返回字符串），
 * 前端就得为每个接口单独写解析逻辑，Axios 拦截器也无法统一处理。
 * 统一之后：code === 200 就是成功，其余交给拦截器弹提示，业务代码只关心 data。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    /** 业务状态码：200 成功，其余为失败 */
    private Integer code;

    /** 提示信息，前端可直接 toast 出来 */
    private String msg;

    /** 业务数据 */
    private T data;

    /* ---------------- 成功 ---------------- */

    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return build(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /* ---------------- 失败 ---------------- */

    public static <T> Result<T> error(String msg) {
        return build(ResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return build(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return build(code, msg, null);
    }

    /** 私有构造工具方法，避免每个静态方法重复 new + set */
    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}

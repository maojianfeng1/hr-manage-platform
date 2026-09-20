package com.hr.common.exception;

import com.hr.common.result.Result;
import com.hr.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 知识点：
 * 1. @RestControllerAdvice = @ControllerAdvice + @ResponseBody，
 *    它拦截所有 @RestController 抛出的异常，集中处理，业务代码里只用 throw，不用 try-catch。
 * 2. 处理顺序：先捕获最具体的异常（如参数校验、唯一键冲突），最后用 Exception 兜底。
 * 3. 所有 handler 都返回 Result，保证前端拿到的永远是统一结构。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 1. 业务异常：返回异常的 code 和 msg */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("[业务异常] {} | {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 2. @RequestBody 参数校验失败（实体字段上的 @NotBlank 等） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + f.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数校验失败：" + msg);
    }

    /** 3. 表单/URL 参数校验失败（@RequestParam 实体上的校验） */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数校验失败：" + msg);
    }

    /** 4. 方法参数（@RequestParam / @PathVariable）上的 @NotNull 等校验失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数校验失败：" + e.getMessage());
    }

    /** 5. 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissing(MissingServletRequestParameterException e) {
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "缺少必填参数：" + e.getParameterName());
    }

    /** 6. 参数类型不匹配（如 id 传了 abc） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "参数类型错误：" + e.getName());
    }

    /** 7. 请求体 JSON 解析失败（如日期格式不对、字段类型错） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleReadable(HttpMessageNotReadableException e) {
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "请求体格式错误，请检查 JSON");
    }

    /** 8. 数据库唯一键冲突（用户名重复、员工编号重复） */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicate(DuplicateKeyException e) {
        log.warn("[唯一键冲突] {}", e.getMessage());
        return Result.error(ResultCode.BUSINESS_ERROR.getCode(), "数据已存在，请勿重复添加");
    }

    /** 9. 兜底：其他所有未预期异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("[系统异常]", e);
        return Result.error(ResultCode.ERROR.getCode(), "系统开小差了，请稍后重试");
    }
}

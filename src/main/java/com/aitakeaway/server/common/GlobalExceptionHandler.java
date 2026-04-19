package com.aitakeaway.server.common;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    /** 请求体 JSON 格式错误或字段类型不匹配 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.error("请求参数格式错误");
    }

    /** URL 路径参数或 @RequestParam 类型转换失败 */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
                       MissingServletRequestParameterException.class})
    public Result<Void> handleParamError(Exception e) {
        return Result.error("请求参数不合法：" + e.getMessage());
    }

    /** 兜底：未预期的异常，避免暴露堆栈 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error("服务器内部错误，请稍后重试");
    }
}

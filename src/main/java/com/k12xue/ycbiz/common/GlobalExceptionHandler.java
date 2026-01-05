package com.k12xue.ycbiz.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e); // 打印错误日志到控制台
        return Result.fail("系统繁忙，请联系管理员：" + e.getMessage());
    }

    // 你还可以专门捕获参数校验异常，我们在讲 Validation 时提到过
}
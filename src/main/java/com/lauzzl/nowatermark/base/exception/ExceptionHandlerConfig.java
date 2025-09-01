package com.lauzzl.nowatermark.base.exception;

import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Hidden
@RestControllerAdvice
public class ExceptionHandlerConfig {


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest req, HttpServletResponse res, Exception e){
        log.error("系统错误", e);
        return Result.failure("系统错误");
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public Object dealMethodArgumentNotValidException(HttpServletResponse res, MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        allErrors.forEach(error -> log.error("参数异常：{}", error.getDefaultMessage()));
        return Result.failure(ErrorCode.METHOD_ARGUMENT_NOT_VALID.code, "参数异常：" + allErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(",")));
    }


    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public Object bizExceptionHandler(HttpServletRequest req, HttpServletResponse res, BizException e){
        return Result.failure(e.getErrorCode(),e.getErrorMsg());
    }

}
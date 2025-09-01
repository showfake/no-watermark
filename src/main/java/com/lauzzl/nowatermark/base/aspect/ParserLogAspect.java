package com.lauzzl.nowatermark.base.aspect;

import com.lauzzl.nowatermark.base.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ParserLogAspect {

    @Pointcut("execution(* com.lauzzl.nowatermark.controller.ParserController.executor(..))")
    public void log(){}


    @Before("log()")
    public void before(JoinPoint joinPoint) {
        log.info("开始解析: {}", joinPoint.getArgs());
    }

    @AfterReturning(value = "log()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Result<?> result) {
        log.info("解析 {} 完成", joinPoint.getArgs());
        log.info("是否成功: {}", result.getCode() == 0);
        log.info("结果: {}", result);
    }

    @AfterThrowing(value = "log()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        log.error("解析 {} 失败", joinPoint.getArgs());
        log.error("异常: {}", e.getMessage(), e);
    }

}

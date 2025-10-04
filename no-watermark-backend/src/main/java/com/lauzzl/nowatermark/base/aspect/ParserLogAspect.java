package com.lauzzl.nowatermark.base.aspect;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;

@Slf4j
@Aspect
@Component
public class ParserLogAspect {

    @Pointcut("execution(* com.lauzzl.nowatermark.controller.ParserController.executor(..))")
    public void parserLog(){}


    @Around("parserLog()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取到HTTP上下文");
            return pjp.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = getIp(request);
        String uuid = UUID.randomUUID().toString();
        String data = JSONUtil.toJsonStr(pjp.getArgs());
        log.info("请求ID: {}, 请求参数: {}, 请求IP: {}", uuid, data, ip);
        try {
            long startTime = System.currentTimeMillis();
            Object result = pjp.proceed();
            long endTime = System.currentTimeMillis();
            log.info("解析完成, 请求ID: {}, 耗时: {}ms, 返回结果: {}", uuid, endTime - startTime, JSONUtil.toJsonStr(result));
            return result;
        } catch (Exception e) {
            log.error("系统错误：{}，请求ID: {}", e.getMessage(), uuid ,e);
            return Result.failure(ErrorCode.SYS_ERROR.code, "解析失败：" + e.getMessage() + ", errorId:" + uuid);
        }
    }

    /**
     * 获取ip
     *
     * @param request 请求
     * @return {@link String }
     */
    private static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                ip = "";
            }
        }
        return ip;
    }
}

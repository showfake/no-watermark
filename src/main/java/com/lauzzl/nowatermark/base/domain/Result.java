package com.lauzzl.nowatermark.base.domain;

import com.lauzzl.nowatermark.base.code.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

/**
 * @author LAUZZL
 * @description: 响应返回体
 */
@Getter
@ToString
@Schema(description = "响应返回体")
public class Result<T> {

    /** 业务错误码 */
    @Schema(description = "业务错误码")
    private Integer code;

    /** 信息描述 */
    @Schema(description = "信息描述")
    private String msg;

    /** 返回参数 */
    @Schema(description = "返回数据")
    private T data;

    private Result(ErrorCode resultStatus, T data) {
        this.code = resultStatus.code;
        this.msg = resultStatus.message;
        this.data = data;
    }

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    /**
     * 返回成功消息
     * @return 成功消息
     */
    public static <T> Result<T> success() {
        return Result.success(ErrorCode.SUCCESS.message);
    }

    /**
     * 返回成功消息
     * @param message 内容
     * @return 成功消息
     */
    public static <T> Result<T> success(String message) {
        return Result.success(message, null);
    }

    /**
     * 返回成功消息
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> Result<T> success(T data) {
        return Result.success(ErrorCode.SUCCESS.message,data);
    }

    /**
     * 返回成功消息
     * @param message 内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> Result<T> success(String message,T data) {
        return new Result<>(ErrorCode.SUCCESS.code, message ,data);
    }

    /**
     * 返回错误消息
     * @return 错误消息
     */
    public static <T> Result<T> failure() {
        return Result.failure(ErrorCode.SYS_ERROR.message);
    }

    /**
     * 返回错误消息
     * @param message 内容
     * @return 错误消息
     */
    public static <T> Result<T> failure(String message) {
        return Result.failure(message,null);
    }

    /**
     * 返回错误消息
     * @param data 数据对象
     * @return 错误消息
     */
    public static <T> Result<T> failure(T data) {
        return Result.failure(ErrorCode.SYS_ERROR.message, data);
    }

    /**
     * 错误消息
     * @param resultStatusEnum 错误码枚举
     * @return 错误消息
     */
    public static <T> Result<T> failure(ErrorCode resultStatusEnum) {
        return Result.failure(resultStatusEnum.code, resultStatusEnum.message);
    }


    /**
     * 返回错误消息
     * @param message 内容
     * @param data 数据对象
     * @return 错误消息
     */
    public static <T> Result<T> failure(String message,T data) {
        return new Result<>(ErrorCode.SYS_ERROR.code, message,data);
    }

    /***
     * 返回自定义状态
     * @param code 状态码
     * @param message 消息
     * @return 消息
     */
    public static <T> Result<T> ok(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
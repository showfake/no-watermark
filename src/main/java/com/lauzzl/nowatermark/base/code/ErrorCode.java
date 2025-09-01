package com.lauzzl.nowatermark.base.code;

import lombok.Getter;
import lombok.ToString;


/**
 * @author LAUZZL
 * @title: ResultStatus
 * @date 2025/03/24 17:26
 * @description: 响应状态
 */
@ToString
@Getter
public enum ErrorCode {
    /**
     * 成功响应
     */
    SUCCESS(0, "操作成功"),

    /**
     * 系统错误
     */
    SYS_ERROR(10001, "系统错误"),
    METHOD_ARGUMENT_NOT_VALID(10002,"参数异常"),

    /**
     * 解析错误
     */
    PARSER_NOT_SUPPORT(10003,"不支持的解析器"),
    PARSER_FAILED(10004,"解析失败"),
    PARSER_NOT_FOUND_MEDIA(10005,"未解析到媒体资源"),
    PARSER_NOT_GET_ID(10006,"获取视频ID失败"),
    PARSER_GET_POST_FAILED(10007,"获取视频信息失败"),
    PARSER_PARSE_MEDIA_INFO_FAILED(10008,"解析媒体信息失败"),
    PARSER_NOT_GET_REAL_URL(10009,"获取真实地址失败"),
    ;


    public Integer code;
    public String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
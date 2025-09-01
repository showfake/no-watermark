package com.lauzzl.nowatermark.base.exception;


import com.lauzzl.nowatermark.base.domain.Result;

/**
 * 异常
 *
 * @author LauZzL
 * @date 2025/08/29
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected Integer code;

    /**
     * 错误信息
     */
    protected String msg;

    public BizException() {
        super();
    }

    public BizException(Result result) {
        super(String.valueOf(result.getCode()));
        this.code = result.getCode();
        this.msg = result.getMsg();
    }

    public BizException(Result result, Throwable cause) {
        super(String.valueOf(result.getCode()), cause);
        this.code = result.getCode();
        this.msg = result.getMsg();
    }

    public BizException(String errorMsg) {
        super(errorMsg);
        this.msg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg) {
        super(String.valueOf(errorCode));
        this.code = errorCode;
        this.msg = errorMsg;
    }

    public BizException(Integer errorCode, String errorMsg, Throwable cause) {
        super(String.valueOf(errorCode), cause);
        this.code = errorCode;
        this.msg = errorMsg;
    }


    public Integer getErrorCode() {
        return code;
    }

    public void setErrorCode(Integer errorCode) {
        this.code = errorCode;
    }

    public String getErrorMsg() {
        return msg;
    }

    public void setErrorMsg(String errorMsg) {
        this.msg = errorMsg;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
package com.lauzzl.nowatermark.base.enums;

public enum WeappOperationTypeEnum {

    API_CALL("API_CALL"),
    AD_VIEW("AD_VIEW"),
    AD_REWARD("AD_REWARD"),
    INVITE("INVITE"),
    LOGIN("LOGIN"),
    ;


    private String value;

    private WeappOperationTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

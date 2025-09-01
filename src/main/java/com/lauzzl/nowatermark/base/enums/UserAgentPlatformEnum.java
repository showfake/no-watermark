package com.lauzzl.nowatermark.base.enums;

import lombok.Getter;

@Getter
public enum UserAgentPlatformEnum {

    DEFAULT(-1),
    PC(0),
    PHONE(1);

    private final Integer type;

    UserAgentPlatformEnum(Integer type) {
        this.type = type;
    }

}

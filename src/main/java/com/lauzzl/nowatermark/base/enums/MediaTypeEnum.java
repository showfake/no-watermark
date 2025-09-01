package com.lauzzl.nowatermark.base.enums;

import lombok.Getter;

@Getter
public enum MediaTypeEnum {

    VIDEO("视频"),
    IMAGE("图集"),
    LIVE("实况"),
    ;

    private final String type;

    MediaTypeEnum(String type) {
        this.type = type;
    }

}

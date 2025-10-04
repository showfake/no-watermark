package com.lauzzl.nowatermark.factory.enums;

import lombok.Getter;

@Getter
public enum MediaTypeEnum {

    VIDEO("视频"),
    IMAGE("图集"),
    LIVE("实况"),
    AUDIO("音频")
    ;

    private final String type;

    MediaTypeEnum(String type) {
        this.type = type;
    }

}

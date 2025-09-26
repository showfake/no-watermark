package com.lauzzl.nowatermark.factory;


import cn.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * 解析器
 *
 * @author LauZzL
 * @date 2025/08/29
 */
@Component
public class ParserFactory {

    /**
     * 视频 url
     */
    private String url;

    /**
     * 设置视频url
     *
     * @param url 网址
     * @return {@link ParserFactory }
     */
    public ParserFactory setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 获取解析器
     *
     * @return {@link Parser }
     */
    public Parser build() {
        for (Platform platform : Platform.getAllPlatforms()) {
            if (url.matches(platform.getRegex())) {
                Parser parser = Optional.ofNullable(SpringUtil.getBean(platform.getParserClass())).orElseThrow(() -> new RuntimeException("解析器不存在"));
                parser.url = url;
                parser.platformName = platform.getPlatformName();
                parser.key = platform.getPlatformName();
                return parser;
            }
        }
        return null;
    }

}

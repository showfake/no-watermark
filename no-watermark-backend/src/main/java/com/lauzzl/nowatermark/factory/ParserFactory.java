package com.lauzzl.nowatermark.factory;


import cn.hutool.core.util.StrUtil;
import com.lauzzl.nowatermark.factory.enums.Platform;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * 解析器
 *
 * @author LauZzL
 * @date 2025/08/29
 */
@Component
public class ParserFactory {

    private final ApplicationContext applicationContext;

    public ParserFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建解析器
     *
     * @param url 网址
     * @return {@link Parser }
     */
    public Parser createParser(String url) {
        if (StrUtil.isBlank(url)) {
            throw new IllegalArgumentException("视频地址不能为空");
        }
        for (Platform platform : Platform.values()) {
            if (url.matches(platform.getRegex())) {
                Class<? extends Parser> parserClass = platform.getParserClass();
                return applicationContext.getBean(parserClass);
            }
        }
        throw new IllegalArgumentException("未找到匹配的解析器，URL: " + url);
    }

}

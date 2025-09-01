package com.lauzzl.nowatermark.base.utils;

import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestResponse;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;

import java.util.Optional;

public class HttpUtil {


    /**
     * 获取重定向 URL
     *
     * @param url 网址
     * @return {@link String }
     */
    public static String getRedirectUrl(String url, UserAgentPlatformEnum userAgentPlatformEnum) {
        ForestResponse response = Forest.get(url)
                .autoRedirects(false)
                .setUserAgent(CommonUtil.getUserAgent(userAgentPlatformEnum))
                .execute(ForestResponse.class);
        return response.getRedirectionLocation();
    }


}

package com.lauzzl.nowatermark.base.utils;

import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestResponse;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;

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
        if (response.statusIs(302)) {
            return getRedirectUrl(response.getRedirectionLocation(), userAgentPlatformEnum);
        }
        return url;
    }


    /**
     * 获取 TTWID，有效期一年
     *
     * @param aid     aid
     * @param service 域名例如: www.toutiao.com
     * @return {@link String } 返回ttwid
     */
    public static String getTtwid(int aid, String service) {
        ForestResponse response = Forest.post("https://ttwid.bytedance.com/ttwid/union/register/")
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .contentTypeJson()
                .addBody("aid", aid)
                .addBody("service", service)
                .addBody("region", "cn")
                .execute(ForestResponse.class);
        if (response.isSuccess() && response.getCookie("ttwid") != null) {
            // cookie 有效期一年
            return response.getCookie("ttwid").getValue();
        }
        return null;
    }

}

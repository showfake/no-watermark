package com.lauzzl.nowatermark.base.utils;

import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;

import java.util.Random;

/**
 * 通用工具类
 * @author LauZzL
 */
public class CommonUtil {

    /**
     * PC端ua
     */
    private static final String[] PC_USER_AGENT = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 OPR/120.0.0.0 (Edition developer)",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.3.1 Safari/605.1.15",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
    };

    /**
     * 手机端ua
     */
    private static final String[] PHONE_USER_AGENT = {
            "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
            "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",
            "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",
            "Mozilla/5.0 (Linux; Android 8.0.0; SM-N9500 Build/R16NW; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.83 Mobile Safari/537.36 T7/10.13 baiduboxapp/10.13.0.11 (Baidu; P1 8.0.0)"
    };

    /**
     * 生成UserAgent
     * @param platform 平台
     * @return userAgent
     */
    public static String getUserAgent(UserAgentPlatformEnum platform) {
        if (platform == null) {
            return null;
        }
        Random random = new Random();
        return switch (platform.getType()) {
            case -1 -> PC_USER_AGENT[0];
            case 0 -> PC_USER_AGENT[random.nextInt(PC_USER_AGENT.length)];
            case 1 -> PHONE_USER_AGENT[random.nextInt(PHONE_USER_AGENT.length)];
            default -> null;
        };
    }


}

package com.lauzzl.nowatermark.base.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

/**
 * url util
 *
 * @author LauZzL
 * @date 2025/08/30
 */
public class UrlUtil {

    /**
     * 获取下一个路径
     *
     * @param urlStr   网址 str
     * @param pathName 路径名称
     * @return {@link String }
     */
    public static String getNextPathSegment(String urlStr, String pathName) {
        if (StrUtil.isBlank(urlStr) || StrUtil.isBlank(pathName)) {
            return null;
        }
        URL url = URLUtil.url(urlStr);
        if (url == null) {
            return null;
        }
        String path = url.getPath();
        String[] segment = path.split("/");
        int index = Arrays.asList(segment).indexOf(pathName);
        return index > -1 && index < segment.length - 1 ? segment[index + 1] : null;
    }

    /**
     * 获取查询值
     *
     * @param urlStr    网址 str
     * @param queryName 查询名称
     * @return {@link String }
     */
    public static String getQueryValue(String urlStr, String queryName) {
        return HttpUtil.decodeParamMap(urlStr, Charset.defaultCharset()).get(queryName);
    }

    /**
     * 获取下一个路径值或查询值
     *
     * @param urlStr    网址 str
     * @param pathName  路径名称
     * @param queryName 查询名称
     * @return {@link String }
     */
    public static String getNextPathSegment(String urlStr, String pathName, String queryName) {
        return Optional.ofNullable(getNextPathSegment(urlStr, pathName))
                .orElse(getQueryValue(urlStr, queryName));
    }

}

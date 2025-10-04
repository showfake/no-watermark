package com.lauzzl.nowatermark.base.utils;

import cn.hutool.core.util.ArrayUtil;
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


    /**
     * 获取最后一条路径
     *
     * @param url 网址
     * @return {@link String }
     */
    public static String getLastPath(String url) {
        if (StrUtil.isBlank(url)) return null;
        String path = URLUtil.url(url).getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }


    public static String getId(String url, String pathName, String queryName) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        if (StrUtil.isNotBlank(pathName)) {
            return UrlUtil.getNextPathSegment(url, pathName);
        }
        if (StrUtil.isNotBlank(queryName)) {
            return UrlUtil.getNextPathSegment(url, null, queryName);
        }
        return null;
    }

    /**
     * 获取帖子ID
     *
     * @param url        帖子 url(该URL需要为最终地址)
     * @param pathNames  路径名称
     * @param queryNames 查询名称
     * @return {@link String }
     */
    public static String getId(String url, String[] pathNames, String[] queryNames) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        if (!ArrayUtil.isEmpty(pathNames)) {
            for (String pathName : pathNames) {
                String nextPathSegment = UrlUtil.getNextPathSegment(url, pathName);
                if (StrUtil.isNotBlank(nextPathSegment)) {
                    return nextPathSegment;
                }
            }
        }
        if (!ArrayUtil.isEmpty(queryNames)) {
            for (String queryName : queryNames) {
                String nextPathSegment = UrlUtil.getNextPathSegment(url, null, queryName);
                if (StrUtil.isNotBlank(nextPathSegment)) {
                    return nextPathSegment;
                }
            }
        }
        return null;
    }


}

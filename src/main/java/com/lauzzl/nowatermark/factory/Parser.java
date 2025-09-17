package com.lauzzl.nowatermark.factory;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import lombok.Getter;

import java.util.Optional;

/**
 * 解析 器
 *
 * @author LauZzL
 * @date 2025/08/29
 */
@Getter
public abstract class Parser {

    /**
     * 视频 url
     */
    protected String url;

    /**
     * 平台名称
     */
    protected String platformName;

    /**
     * 缓存key
     */
    protected String key;


    /**
     * 判断封面是否为空，为空则设置第一张图片为封面
     *
     * @param resp 回复
     */
    public static void resetCover(ParserResp resp) {
        Optional.ofNullable(resp).ifPresent(e->{
            if (StrUtil.isBlank(e.getCover()) && ObjectUtil.isNotEmpty(e.getMedias())) {
                e.getMedias().stream().filter(m -> m.getType() == MediaTypeEnum.IMAGE).findFirst().ifPresent(m -> e.setCover(m.getUrl()));
            }
        });
    }

    /**
     * 获取帖子ID(未获取到会自动获取重定向地址)
     *
     * @param url       帖子 url
     * @param pathName  路径名称
     * @return {@link String }
     */
    public static String getId(String url, UserAgentPlatformEnum userAgentPlatformEnum, String pathName) {
        return Optional.ofNullable(UrlUtil.getNextPathSegment(url, pathName))
                .orElseGet(() -> UrlUtil.getNextPathSegment(HttpUtil.getRedirectUrl(url, userAgentPlatformEnum), pathName));
    }


    /**
     * 获取帖子ID(未获取到会自动获取重定向地址)
     *
     * @param url       帖子 url
     * @param pathName  路径名称
     * @param queryName 查询名称
     * @return {@link String }
     */
    public static String getId(String url, UserAgentPlatformEnum userAgentPlatformEnum, String pathName, String queryName) {
        return Optional.ofNullable(UrlUtil.getNextPathSegment(url, pathName, queryName))
                .orElseGet(() -> UrlUtil.getNextPathSegment(HttpUtil.getRedirectUrl(url, userAgentPlatformEnum), pathName, queryName));
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


    public abstract Result<ParserResp> execute() throws Exception;
}

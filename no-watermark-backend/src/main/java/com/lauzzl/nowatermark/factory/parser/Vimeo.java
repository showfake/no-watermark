package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestRequest;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.config.ProxyConfig;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import com.lauzzl.nowatermark.factory.Parser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Vimeo implements Parser {

    @Resource
    private ProxyConfig proxyConfig;

    @Value("${redis.cache.cookie-key}")
    private String cookieKey;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String authorization = getAuthorization();
        if (StrUtil.isBlank(authorization)) {
            return Result.failure(ErrorCode.PARSER_COOKIE_EXPIRED);
        }
        ForestRequest<?> request = Forest.get(url)
                .setUserAgent("1")
                .setProxy(proxyConfig.proxy())
                .setHost("vimeo.com");
        String response = request.executeAsString();
        String vid = UrlUtil.getLastPath(url);
        if (StrUtil.isBlank(vid)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        String signature = ReUtil.get(String.format("video-signature-%s\" content=\"(.*?)\"", vid), response, 1);
        if (StrUtil.isBlank(signature)) {
            log.error("{} 获取signature失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        String data = request.url(String.format("https://api.vimeo.com/videos/%s?anon_signature=%s&fields=download", vid, signature))
                .addHeader("Accept", "application/vnd.vimeo.*+json;version=3.4.12")
                .addHeader("Authorization", authorization)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .executeAsString();
        if (!StrUtil.contains(data, "download")) {
            log.error("{} 获取data失败，返回结果：{}", url, data);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        return extract(response, data);
    }

    private String getAuthorization() {
        String key = this.getClass().getSimpleName();
        String cookie = redisTemplate.opsForValue().get(String.format(cookieKey, key));
        if (StrUtil.isNotBlank(cookie)) {
            return cookie;
        }
        String response = Forest.get("https://vimeo.com/_next/viewer").proxy(proxyConfig.proxy()).executeAsString();
        String authorization = ReUtil.get("jwt\":\"(.*?)\"", response, 1);
        if (StrUtil.isBlank(authorization)) {
            return null;
        }
        authorization = "jwt " + authorization;
        redisTemplate.opsForValue().set(String.format(cookieKey, key), authorization, 1, TimeUnit.DAYS);
        return authorization;
    }

    private Result<ParserResp> extract(String response, String data) {
        ParserResp result = new ParserResp();
        JSONObject jsonObject = JSONUtil.parseObj(data);
        extractInfo(response, result);
        extractMedia(jsonObject, result);
        return Result.success(result);
    }

    private void extractMedia(JSONObject jsonObject, ParserResp result) {
        Optional.ofNullable(jsonObject.getJSONArray("download")).ifPresent(node -> node.toList(JSONObject.class).forEach(media -> {
            result.getMedias().add(new ParserResp.Media()
                    .setType(MediaTypeEnum.VIDEO)
                    .setUrl(media.getStr("link"))
                    .setHeight(media.getInt("height"))
                    .setWidth(media.getInt("width"))
            );
        }));
    }

    private void extractInfo(String response, ParserResp result) {
        String title = ReUtil.get("title\" content=\"(.*?)\"", response, 1);
        String nickname = ReUtil.get("Person\",\"name\":\"(.*?)\"", response, 1);
        String avatar = ReUtil.get("\"image\":\"(.*?)\"", response, 1);
        result.setTitle(title);
        result.setAuthor(new ParserResp.Author()
                .setNickname(nickname)
                .setAvatar(avatar)
        );
    }

}

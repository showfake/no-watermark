package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestCookie;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JinRiTouTiao extends Parser {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${redis.cache.cookie-key}")
    private String cookieKey;

    @Override
    public Result<ParserResp> execute() throws Exception {
        String ttwid = redisTemplate.opsForValue().get(String.format(cookieKey, key));
        if (StrUtil.isBlank(ttwid)) {
            ttwid = HttpUtil.getTtwid(24, "www.toutiao.com");
            if (StrUtil.isBlank(ttwid)) {
                return Result.failure(ErrorCode.PARSER_COOKIE_EXPIRED);
            }
            redisTemplate.opsForValue().set(String.format(cookieKey, key), ttwid, 365, TimeUnit.DAYS);
        }
        url = HttpUtil.getRedirectUrl(url, UserAgentPlatformEnum.DEFAULT);
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .addCookie(new ForestCookie("ttwid", ttwid), false)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(response);
    }

    private Result<ParserResp> extract(String response) throws UnsupportedEncodingException {
        if (!StrUtil.contains(response, "RENDER_DATA")) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        ParserResp resp = new ParserResp();
        String data = ReUtil.get("id=\"RENDER_DATA\" type=\"application/json\">(.*?)</script>", response, 1);
        // utf8解码
        data = URLDecoder.decode(data, StandardCharsets.UTF_8);
        if (StrUtil.isBlank(data)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        JSONObject jsonObject = JSONUtil.parseObj(data);
        JSONObject videoObject = jsonObject.getByPath("data.initialVideo", JSONObject.class);
        extractVideo(videoObject, resp);
        extractInfo(jsonObject, resp);
        return Result.success(resp);
    }

    private void extractInfo(JSONObject jsonObject, ParserResp resp) {
        resp.setTitle(jsonObject.getByPath("data.initialVideo.title", String.class));
        resp.getAuthor()
                .setNickname(jsonObject.getByPath("data.initialVideo.userInfo.name", String.class))
                .setAvatar(jsonObject.getByPath("data.initialVideo.userInfo.avatarUrl", String.class));
        resp.setCover(jsonObject.getByPath("data.initialVideo.coverUrl", String.class));
    }

    private void extractVideo(JSONObject videoObject, ParserResp resp) {
        Optional.ofNullable(videoObject.getByPath("videoPlayInfo['video_list']", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(v -> {
            resp.getMedias().add(new ParserResp.Media()
                    .setType(MediaTypeEnum.VIDEO)
                    .setUrl(v.getStr("main_url"))
                    .setHeight(v.getByPath("['video_meta'].vheight", Integer.class))
                    .setWidth(v.getByPath("['video_meta'].vwidth", Integer.class))
            );
        }));
    }
}

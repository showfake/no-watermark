package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestCookie;
import com.dtflys.forest.http.ForestResponse;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WeiBo implements Parser {

    private final static String BASE_URL = "https://weibo.com/ajax/statuses/show";

    @Value("${redis.cache.cookie-key}")
    private String cookieKey;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String id = UrlUtil.getLastPath(url);
        if (StrUtil.isBlank(id)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        String response = Forest.get(BASE_URL)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .addQuery("id", id)
                .addCookie(new ForestCookie("SUB", getCookieSub(false)))
                // 403
                .addHeader("Referer", url)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        // 如果cookie过期则重新获取
        if (StrUtil.contains(response, "登录")) {
            getCookieSub(true);
            return Result.failure(ErrorCode.PARSER_COOKIE_EXPIRED);
        }
        return extract(response);
    }

    private Result<ParserResp> extract(String response) {
        JSONObject jsonObject = JSONUtil.parseObj(response);
        ParserResp result = new ParserResp();
        extractInfo(jsonObject, result);
        extractVideo(jsonObject, result);
        extractImage(jsonObject, result);
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }

    private void extractImage(JSONObject jsonObject, ParserResp result) {
        Optional.ofNullable(jsonObject.get("pic_infos", JSONObject.class)).ifPresent(node -> {
            node.forEach(item -> {
                JSONObject pic = (JSONObject) item.getValue();
                List<String> keys = List.of(new String[]{"thumbnail", "bmiddle", "large", "original", "largest", "mw2000", "largecover"});
                keys.forEach(key -> result.getMedias().add(new ParserResp.Media()
                        .setType(MediaTypeEnum.IMAGE)
                        .setUrl(pic.getByPath(key + ".url", String.class))
                        .setHeight(pic.getByPath(key + ".height", Integer.class))
                        .setWidth(pic.getByPath(key + ".width", Integer.class))
                ));
            });
        });
    }

    private void extractVideo(JSONObject jsonObject, ParserResp result) {
        Optional.ofNullable(jsonObject.getByPath("['page_info']['media_info']['playback_list']", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(media -> {
            result.getMedias().add(new ParserResp.Media()
                    .setType(MediaTypeEnum.VIDEO)
                    .setUrl(media.getByPath("['play_info'].url", String.class))
                    .setHeight(media.getByPath("['play_info'].height", Integer.class))
                    .setWidth(media.getByPath("['play_info'].width", Integer.class))
            );
        }));
    }

    private void extractInfo(JSONObject jsonObject, ParserResp result) {
        result.setTitle(jsonObject.getStr("text_raw"));
        result.getAuthor()
                .setNickname(jsonObject.getByPath("user['screen_name']", String.class))
                .setAvatar(jsonObject.getByPath("user['profile_image_url']", String.class));
        result.setCover(jsonObject.getByPath("['page_info']['page_pic']", String.class));
    }


    /**
     * 获取 cookie sub值
     * 该cookie有效期一年
     *
     * @param isExpire 是否过期
     * @return {@link String }
     */
    private String getCookieSub(boolean isExpire) {
        String key = this.getClass().getSimpleName();
        if (isExpire) {
            redisTemplate.delete(String.format(cookieKey, key));
        }
        String cookie = redisTemplate.opsForValue().get(String.format(cookieKey, key));
        if (StrUtil.isNotBlank(cookie)) {
            return cookie;
        }
        ForestResponse response =  Forest.post("https://passport.weibo.com/visitor/genvisitor2")
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .contentFormUrlEncoded()
                .addBody("cb", "visitor_gray_callback")
                .execute(ForestResponse.class);
        if (response.isSuccess()) {
            // cookie 有效期一年
            cookie = response.getCookie("SUB").getValue();
            redisTemplate.opsForValue().set(String.format(cookieKey, key), cookie, 365, TimeUnit.DAYS);
            return cookie;
        }
        return null;
    }


}

package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.net.RFC3986;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestResponse;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.CryptoUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@Slf4j
public class Weverse extends Parser {

    private static final String BASE_URL = "https://global.apis.naver.com/weverse/wevweb/post/v1.0/post-%s";
    private static final String APP_ID = "be4d79eb8fc7bd008ee82c8ec4ff6fd4";
    private static final String ENCRYPT_KEY = "1b9cb6378d959b45714bec49971ade22e6e24e42";
    private static final String ENCRYPT_DATA = "/post/v1.0/post-%s?appId=be4d79eb8fc7bd008ee82c8ec4ff6fd4&fieldSet=postV1&fields=recommendProductSlot&gcc=US&language=zh-cn&os=WEB&platform=WEB&wpf=pc%s";
    private static final HmacAlgorithm ENCRYPT_ALGORITHM = HmacAlgorithm.HmacSHA1;
    private static final String ENCRYPT_OUTPUT = "base64";

    @Override
    public Result<ParserResp> execute() throws Exception {
        String postId = getId(url, "media", null);
        if (StrUtil.isBlank(postId)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String wmd = CryptoUtil.Hmac(String.format(ENCRYPT_DATA, postId, timestamp), ENCRYPT_KEY.getBytes(), ENCRYPT_ALGORITHM, ENCRYPT_OUTPUT, Charset.defaultCharset());
        wmd = RFC3986.QUERY_PARAM_VALUE_STRICT.encode(wmd, StandardCharsets.UTF_8);
        ForestResponse response = Forest.get(String.format(BASE_URL, postId))
                .addQuery("appId", APP_ID)
                .addQuery("fieldSet", "postV1")
                .addQuery("fields", "recommendProductSlot")
                .addQuery("gcc", "US")
                .addQuery("language", "zh-cn")
                .addQuery("os", "WEB")
                .addQuery("platform", "WEB")
                .addQuery("wpf", "pc")
                .addQuery("wmd", wmd)
                .addQuery("wmsgpad", timestamp)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .addHeader("referer", "https://weverse.io/")
                .execute(ForestResponse.class);
        return extract(response.readAsString());
    }

    private Result<ParserResp> extract(String response) {
        JSONObject jsonObject = JSONUtil.parseObj(response);
        if (jsonObject.isEmpty() || !"common_700".equals(jsonObject.getStr("errorCode"))) {
            log.error("解析失败: {}", response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        ParserResp result = new ParserResp();
        JSONObject data = jsonObject.getJSONObject("data");
        extractInfo(data, result);
        extractImage(data, result);
        extractVideo(data, result);
        resetCover(result);
        return Result.success(result);
    }

    private void extractImage(JSONObject jsonObject, ParserResp result) {
        Optional.ofNullable(jsonObject.getByPath("extension.image.photos", JSONArray.class)).ifPresent(photos->photos.toList(JSONObject.class).forEach(photo->{
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(photo.getStr("url"))
                    .setWidth(photo.getInt("width"))
                    .setHeight(photo.getInt("height"))
                    .setType(MediaTypeEnum.IMAGE)
            );
        }));
    }

    private void extractVideo(JSONObject jsonObject, ParserResp result) {
    }

    private void extractInfo(JSONObject jsonObject, ParserResp result) {
        result.setTitle(jsonObject.getStr("title"));
        result.getAuthor()
                .setNickname(jsonObject.getByPath("author.profileName", String.class))
                .setAvatar(jsonObject.getByPath("author.profileImageUrl", String.class));
        result.setCover(jsonObject.getByPath("extension.mediaInfo.thumbnail.url", String.class));
    }
}

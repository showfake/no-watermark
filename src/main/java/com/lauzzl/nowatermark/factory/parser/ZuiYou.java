package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ZuiYou extends Parser {

    private static final String BASE_URL = "https://share.xiaochuankeji.cn/planck/share/post/detail_h5";
    private static final String VERSION = "5.2.13.011";

    @Override
    public Result<ParserResp> execute() throws Exception {
        String id = getId(url, UserAgentPlatformEnum.PHONE, null, "pid");
        String response = Forest.post(BASE_URL)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .contentTypeJson()
                .addBody("pid", Integer.valueOf(id))
                .addBody("h_av", VERSION)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        return extract(response);
    }

    private Result<ParserResp> extract(String content) {
        ParserResp result = new ParserResp();
        JSONObject jsonObject = JSONUtil.parseObj(content);
        JSONObject dataObject = jsonObject.getByPath("data.post", JSONObject.class);
        if (dataObject == null || dataObject.isEmpty()) {
            log.error("解析链接：{} 失败，返回结果：{}", url, content);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        extractInfo(dataObject, result);
        extractImage(dataObject, result);
        extractVideo(dataObject, result);
        resetCover(result);
        return Result.success(result);
    }

    private void extractVideo(JSONObject dataObject, ParserResp result) {
        Optional.ofNullable(dataObject.get("videos", JSONObject.class)).ifPresent(node -> {
            String key = node.keySet().stream().findFirst().orElse(null);
            if (StrUtil.isNotBlank(key)) {
                JSONObject videoObject = node.get(key, JSONObject.class);
                result.getMedias().add(new ParserResp.Media()
                        .setType(MediaTypeEnum.VIDEO)
                        .setUrl(videoObject.getStr("url"))
                );
            }

        });
    }

    private void extractImage(JSONObject dataObject, ParserResp result) {
        Optional.ofNullable(dataObject.get("imgs", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> result.getMedias().add(
                new ParserResp.Media()
                        .setType(MediaTypeEnum.IMAGE)
                        .setUrl(image.getByPath("urls['540_webp'].urls[0]", String.class))
                        .setHeight(image.getInt("h"))
                        .setWidth(image.getInt("w"))
        )));
    }

    private void extractInfo(JSONObject dataObject, ParserResp result) {
        result.setTitle(dataObject.getStr("content"));
        result.setAuthor(result.getAuthor()
                .setNickname(dataObject.getByPath("member.name", String.class))
                .setAvatar(dataObject.getByPath("member['avatar_urls']['aspect_low'].urls[0]", String.class))
        );
    }
}

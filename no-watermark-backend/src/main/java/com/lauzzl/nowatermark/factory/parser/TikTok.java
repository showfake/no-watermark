package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestProxy;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.config.ProxyConfig;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.factory.Parser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class TikTok implements Parser {

    @Resource
    private ProxyConfig proxyConfig;

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        ForestProxy proxy = proxyConfig.proxy();
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .proxy(proxy)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        String jsonData = ReUtil.get("webapp.video-detail\":(.*?),\"webapp.a-b\":", response, 1);
        if (StrUtil.isBlank(jsonData)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(jsonData);
    }

    private Result<ParserResp> extract(String jsonData) {
        ParserResp result = new ParserResp();
        JSONObject jsonObj = JSONUtil.parseObj(jsonData);
        // itemInfo.itemStruct
        JSONObject item = jsonObj.getByPath("itemInfo.itemStruct", JSONObject.class);
        extractInfo(item, result);
        extractVideo(item, result);
        extractImage(item, result);
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }

    private void extractImage(JSONObject item, ParserResp result) {
        // TODO
    }

    private void extractVideo(JSONObject item, ParserResp result) {
        Optional.ofNullable(item.get("video", JSONObject.class)).ifPresent(node -> {
            result.getMedias().add(
                    new ParserResp.Media()
                            .setUrl(node.getStr("playAddr"))
                            .setType(MediaTypeEnum.VIDEO)
                            .setHeight(node.getInt("height"))
                            .setWidth(node.getInt("width"))
            );
            result.setCover(node.getStr("cover"));
        });
    }

    private void extractInfo(JSONObject item, ParserResp result) {
        result.setTitle(item.getStr("desc"));
        result.getAuthor()
                .setNickname(item.getByPath("author.nickname", String.class))
                .setAvatar(item.getByPath("author.avatarThumb", String.class));
    }
}

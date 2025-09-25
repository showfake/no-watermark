package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
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
public class AcFun extends Parser {
    @Override
    public Result<ParserResp> execute() throws Exception {
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        return extract(response);
    }

    private Result<ParserResp> extract(String response) {
        String data = ReUtil.get("window.videoInfo = (.*?);\\n", response, 1);
        if (StrUtil.isBlank(data)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        ParserResp resp = new ParserResp();
        JSONObject jsonObj = JSONUtil.parseObj(data);
        extractInfo(jsonObj, resp);
        extractVideo(jsonObj, resp);
        extractImage(jsonObj, resp);
        resetCover(resp);
        return Result.success(resp);
    }

    private void extractInfo(JSONObject item, ParserResp result) {
        result.setTitle(item.getStr("title"));
        result.getAuthor()
                .setNickname(item.getByPath("user.name", String.class))
                .setAvatar(item.getByPath("user.headUrl", String.class));
        result.setCover(item.getByPath("coverImgInfo.thumbnailImageCdnUrl", String.class));
    }

    private void extractVideo(JSONObject item, ParserResp result) {
        Optional.ofNullable(item.getByPath("currentVideoInfo.ksPlayJson", String.class)).ifPresent(s -> {
            JSONObject ksPlayObj = JSONUtil.parseObj(s);
            ksPlayObj.get("adaptationSet", JSONArray.class).toList(JSONObject.class).forEach(adaptationSet -> {
                adaptationSet.get("representation", JSONArray.class).toList(JSONObject.class).forEach(representation -> {
                    result.getMedias().add(new ParserResp.Media()
                            .setType(MediaTypeEnum.VIDEO)
                            .setUrl(representation.getStr("url"))
                            .setHeight(representation.getInt("height"))
                            .setWidth(representation.getInt("width"))
                    );
                });
            });
        });
    }

    private void extractImage(JSONObject item, ParserResp result) {
    }
}

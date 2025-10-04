package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
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
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class WeiShi implements Parser {

    private static final String BASE_URL = "https://h5.weishi.qq.com/webapp/json/weishi/WSH5GetPlayPage";

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String id = UrlUtil.getId(url,null, "id");
        String response = Forest.post(BASE_URL)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .contentTypeJson()
                .addBody("feedid", id)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        return extract(response);
    }


    private Result<ParserResp> extract(String content) {
        ParserResp result = new ParserResp();
        JSONObject jsonObject = JSONUtil.parseObj(content);
        JSONObject itemObject = jsonObject.getByPath("data.feeds[0]", JSONObject.class);
        extractInfo(itemObject, result);
        extractVideo(itemObject, result);
        extractImage(itemObject, result);
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }

    private void extractImage(JSONObject itemObject, ParserResp result) {
        Optional.ofNullable(itemObject.get("images", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
            result.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.IMAGE)
                            .setUrl(image.getStr("url"))
                            .setHeight(image.getInt("height"))
                            .setWidth(image.getInt("width"))
            );
        }));
    }

    private void extractVideo(JSONObject itemObject, ParserResp result) {
        Optional.ofNullable(itemObject.get("video", JSONObject.class)).ifPresent(node -> {
            result.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.VIDEO)
                            .setUrl(itemObject.getStr("video_url"))
                            .setHeight(node.getInt("height"))
                            .setWidth(node.getInt("width"))
            );
        });
    }

    private void extractInfo(JSONObject itemObject, ParserResp result) {
        result.setTitle(itemObject.getStr("feed_desc"));
        result.getAuthor()
                .setNickname(itemObject.getByPath("poster.nick", String.class))
                .setAvatar(itemObject.getByPath("poster.avatar", String.class));
        Optional.ofNullable(itemObject.getByPath("video_cover.static_cover.url", String.class)).ifPresent(result::setCover);
    }

}

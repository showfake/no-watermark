package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
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
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class KuaiShou implements Parser {


    private static final String AUTHOR_PREFIX = "VisionVideoDetailAuthor:";
    private static final String VIDEO_DETAIL_PREFIX = "VisionVideoDetailPhoto:";

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .autoRedirects(true)
                .executeAsString();
        if (StrUtil.isBlank(response) || !StrUtil.containsAny(response, "__APOLLO_STATE__", "INIT_STATE")) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        String videoJsonStr = ReUtil.get("__APOLLO_STATE__=(.*?);\\(function", response, 1);
        String imageJsonStr = ReUtil.get("INIT_STATE = (.*?)</script>", response, 1);
        if (StrUtil.isAllBlank(videoJsonStr, imageJsonStr)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(videoJsonStr, imageJsonStr);
    }


    private Result<ParserResp> extract(String videoJsonStr, String imageJsonStr) {
        ParserResp result = new ParserResp();
        if (StrUtil.isNotBlank(videoJsonStr)) {
            JSONObject videoObject = JSONUtil.parseObj(videoJsonStr);
            videoObject = videoObject.get("defaultClient", JSONObject.class);
            extractVideo(videoObject, result);
        }
        if (StrUtil.isNotBlank(imageJsonStr)) {
            JSONObject imageObject = JSONUtil.parseObj(imageJsonStr);
            extractImage(imageObject, result);
        }
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }



    private void extractVideo(JSONObject obj, ParserResp resp) {
        // 获取所有key
        Set<String> keys = obj.keySet();
        // 获取作者key
        String authorKey = keys.stream()
                .filter(key -> key.startsWith(AUTHOR_PREFIX))
                .findFirst()
                .orElseThrow(()->new IllegalArgumentException("未获取到作者ID"));
        // 获取作品key
        String videoDetailKey = keys.stream()
                .filter(key -> key.startsWith(VIDEO_DETAIL_PREFIX))
                .findFirst()
                .orElseThrow(()->new IllegalArgumentException("未获取到作品ID"));
        // 设置作者信息
        resp.getAuthor()
                .setNickname(obj.get(authorKey, JSONObject.class).getStr("name"))
                .setAvatar(obj.get(authorKey, JSONObject.class).getStr("headerUrl"));
        // 获取视频信息
        JSONObject videoDetail = obj.get(videoDetailKey, JSONObject.class);
        resp.setTitle(videoDetail.getStr("caption"));
        resp.setCover(videoDetail.getStr("coverUrl"));
        // video -> videoDetail.videoResource.json.h264.adaptationSet[0].representation[0].backupUrl[0]
        JSONObject representation = videoDetail.getByPath("videoResource.json.h264.adaptationSet[0].representation[0]", JSONObject.class);
        resp.getMedias().add(new ParserResp.Media()
                .setType(MediaTypeEnum.VIDEO)
                .setUrl(representation.getByPath("backupUrl[0]", String.class))
                .setHeight(representation.getInt("height"))
                .setWidth(representation.getInt("width"))
        );
    }


    private void extractImage(JSONObject obj, ParserResp resp) {
        Set<String> keys = obj.keySet();
        JSONObject imageObj = keys.stream()
                .map(key -> obj.get(key, JSONObject.class))
                .filter(o -> Objects.nonNull(o.get("atlas")))
                .findFirst()
                .orElse(null);
        if (imageObj == null || imageObj.isEmpty()) {
            return;
        }
        resp.setTitle(imageObj.getByPath("photo.caption", String.class));
        resp.setCover(imageObj.getByPath("photo.coverUrls[0].url", String.class));
        resp.getAuthor()
                .setNickname(imageObj.getByPath("photo.userName", String.class))
                .setAvatar(imageObj.getByPath("photo.headUrl", String.class));
        String cdn = imageObj.getByPath("atlas.cdnList[0].cdn", String.class);
        // image_uri -> atlas.list
        Iterator<String> imageUrlIterator = imageObj.getByPath("atlas.list", JSONArray.class).toList(String.class).stream().map(url -> "https://" + cdn + url).toList().iterator();
        Iterator<JSONObject> sizeListIterator = imageObj.getByPath("atlas.size", JSONArray.class).toList(JSONObject.class).iterator();
        while (imageUrlIterator.hasNext() && sizeListIterator.hasNext()) {
            resp.getMedias().add(new ParserResp.Media()
                    .setType(MediaTypeEnum.IMAGE)
                    .setUrl(imageUrlIterator.next())
                    .setHeight(sizeListIterator.next().getInt("h"))
                    .setWidth(sizeListIterator.next().getInt("w"))
            );
        }
    }
}

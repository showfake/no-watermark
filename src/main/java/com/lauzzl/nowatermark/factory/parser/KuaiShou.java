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

import java.util.*;

@Component
@Slf4j
public class KuaiShou extends Parser {


    private static final String AUTHOR_PREFIX = "VisionVideoDetailAuthor:";
    private static final String VIDEO_DETAIL_PREFIX = "VisionVideoDetailPhoto:";

    @Override
    public Result<ParserResp> execute() throws Exception {
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .autoRedirects(true)
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(response);
    }


    private Result<ParserResp> extract(String content) {
        if (!StrUtil.containsAny(content, "__APOLLO_STATE__", "INIT_STATE")) {
            log.error("解析链接：{} 失败，返回结果：{}", url, content);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        // 获取json信息
        // video_client -> defaultClient
        // video_author_name -> video_client['VisionVideoDetailAuthor:${authorId}'].name
        // video_author_avatar -> video_client['VisionVideoDetailAuthor:${authorId}'].headerUrl
        String videoJsonData = ReUtil.get("__APOLLO_STATE__=(.*?);\\(function", content, 1);
        String imageJsonData = ReUtil.get("INIT_STATE = (.*?)</script>", content, 1);
        if (StrUtil.isAllBlank(videoJsonData, imageJsonData)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, content);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        ParserResp result = new ParserResp();
        // 处理视频
        Optional.ofNullable(videoJsonData).ifPresent(v -> {
            JSONObject videoObject = JSONUtil.parseObj(v);
            videoObject = videoObject.get("defaultClient", JSONObject.class);
            extractVideo(videoObject, result);
        });
        // 处理图片
        Optional.ofNullable(imageJsonData).ifPresent(v -> {
            JSONObject imageObject = JSONUtil.parseObj(v);
            extractImage(imageObject, result);
        });
        resetCover(result);
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
                .setResolution(String.format("%sx%s", representation.get("width"), representation.get("height")))
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
        Iterator<String> sizeListIterator = imageObj.getByPath("atlas.size", JSONArray.class).toList(JSONObject.class).stream().map(size -> String.format("%sx%s", size.getStr("w"), size.getStr("h"))).toList().iterator();
        while (imageUrlIterator.hasNext() && sizeListIterator.hasNext()) {
            resp.getMedias().add(new ParserResp.Media()
                    .setType(MediaTypeEnum.IMAGE)
                    .setUrl(imageUrlIterator.next())
                    .setResolution(sizeListIterator.next())
            );
        }
    }
}

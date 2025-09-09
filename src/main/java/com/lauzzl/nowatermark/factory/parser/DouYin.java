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
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Struct;
import java.util.Optional;

@Slf4j
@Component
public class DouYin extends Parser {

    // 视频图集
    private static final String VIDEO_URL = "https://www.iesdouyin.com/share/video/{}";

    // 图集实况
    private static final String SLIDE_URL = "https://www.iesdouyin.com/web/api/v2/aweme/slidesinfo/?reflow_source=reflow_page&web_id={}&device_id={}&from_did=&user_cip=&aweme_ids=%5B{}%5D&request_source=200";

    @Override
    public Result<ParserResp> execute() throws Exception {
        String redirectUrl = HttpUtil.getRedirectUrl(url, UserAgentPlatformEnum.PHONE);
        boolean isSlide = isSlide(redirectUrl);
        String id = isSlide ? getId(url, UserAgentPlatformEnum.PHONE, "slides", "mid") : getId(url, UserAgentPlatformEnum.PHONE, "video", "modal_id");
        if (StrUtil.isBlank(id)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        String url = StrUtil.replace(isSlide ? SLIDE_URL: VIDEO_URL, "{}", id);
        String content =  Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.PHONE))
                .executeAsString();
        if (!content.contains(id)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, content);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        return extract(content, isSlide);
    }


    private boolean isSlide(String url) {
        return url.contains("share/slides");
    }


    private Result<ParserResp> extract(String content, boolean isSlide) {
        JSONObject jsonObj,item;
        ParserResp result = new ParserResp();
        if (isSlide) {
            jsonObj = JSONUtil.parseObj(content);
            item = jsonObj.getByPath("['aweme_details'][0]", JSONObject.class);
        } else {
            String jsonData = ReUtil.get("_ROUTER_DATA = (.*?)</script", content, 1);
            if (StrUtil.isBlank(jsonData)) {
                log.error("解析链接：{} 失败，返回结果：{}", url, content);
                return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
            }
            jsonObj = JSONUtil.parseObj(jsonData);
            item = jsonObj.getByPath("loaderData['video_(id)/page'].videoInfoRes['item_list'][0]", JSONObject.class);
        }
        if (item == null || item.isEmpty()) {
            log.error("解析链接：{} 失败，返回结果：{}", url, content);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        extractInfo(item, result);
        extractVideo(item, result);
        extractImage(item, result);
        resetCover(result);
        return Result.success(result);
    }


    private void extractInfo(JSONObject obj, ParserResp resp) {
        resp.setTitle(obj.getStr("desc"));
        resp.getAuthor()
                .setNickname(obj.getByPath("author.nickname", String.class))
                .setAvatar(obj.getByPath("author.avatar_thumb.url_list.0", String.class));
    }

    private void extractVideo(JSONObject obj, ParserResp resp) {
        resp.setCover(obj.getByPath("video.cover.url_list.0", String.class));
        Optional.ofNullable(obj.getByPath("video", JSONObject.class)).ifPresent(node -> {
            String resolution = String.format("%sx%s", node.getStr("width"), node.getStr("height"));
            node.getByPath("play_addr.url_list", JSONArray.class).toList(String.class).forEach(urlNode -> resp.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.VIDEO)
                            // 去掉水印
                            .setUrl(urlNode.replace("playwm", "play"))
                            .setResolution(resolution)
            ));
        });
    }

    private void extractImage(JSONObject obj, ParserResp resp) {
        Optional.ofNullable(obj.getByPath("images", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
            resp.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.IMAGE)
                            .setUrl(image.getByPath("url_list.0", String.class))
                            .setResolution(String.format("%sx%s", image.get("width"), image.get("height")))
            );
            JSONObject video = image.getJSONObject("video");
            if (video != null) {
                resp.getMedias().add(
                        new ParserResp.Media()
                                .setType(MediaTypeEnum.LIVE)
                                .setUrl(video.getByPath("['play_addr']['url_list'][0]", String.class))
                                .setResolution(String.format("%sx%s", video.get("width"), video.get("height")))
                );
            }
        }));
    }
}

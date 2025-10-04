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
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class DouYin implements Parser {

    // 视频图集
    private static final String VIDEO_URL = "https://www.iesdouyin.com/share/video/{}";

    // 图集实况
    private static final String SLIDE_URL = "https://www.iesdouyin.com/web/api/v2/aweme/slidesinfo/?reflow_source=reflow_page&web_id={}&device_id={}&from_did=&user_cip=&aweme_ids=%5B{}%5D&request_source=200";

    private static final String[] PATH_NAMES = new String[]{"slides", "video", "note"};

    private static final String[] QUERY_NAMES = new String[]{"mid", "modal_id"};

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String redirectUrl = HttpUtil.getRedirectUrl(url, UserAgentPlatformEnum.PHONE);
        boolean isSlide = isSlide(redirectUrl);
        String id = UrlUtil.getId(redirectUrl, PATH_NAMES, QUERY_NAMES);
        if (StrUtil.isBlank(id)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        String reqUrl = StrUtil.replace(isSlide ? SLIDE_URL: VIDEO_URL, "{}", id);
        String response =  Forest.get(reqUrl)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.PHONE))
                .executeAsString();
        if (!response.contains(id)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        JSONObject itemObj = getItemObj(response, isSlide);
        if (itemObj == null || itemObj.isEmpty()) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        return extract(itemObj);
    }


    private JSONObject getItemObj(String content, boolean isSlide) {
        JSONObject jsonObj,item;
        if (isSlide) {
            jsonObj = JSONUtil.parseObj(content);
            item = jsonObj.getByPath("['aweme_details'][0]", JSONObject.class);
        } else {
            String jsonData = ReUtil.get("_ROUTER_DATA = (.*?)</script", content, 1);
            jsonObj = JSONUtil.parseObj(jsonData);
            item = jsonObj.getByPath("loaderData['video_(id)/page'].videoInfoRes['item_list'][0]", JSONObject.class);
        }
        return item;
    }


    private boolean isSlide(String url) {
        return url.contains("share/slides");
    }


    private Result<ParserResp> extract(JSONObject itemObj) {
        ParserResp result = new ParserResp();
        extractInfo(itemObj, result);
        extractVideo(itemObj, result);
        extractImage(itemObj, result);
        ParserResultUtils.resetCover(result);
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
            node.getByPath("play_addr.url_list", JSONArray.class).toList(String.class).forEach(urlNode -> resp.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.VIDEO)
                            // 去掉水印
                            .setUrl(urlNode.replace("playwm", "play"))
                            .setHeight(node.getInt("height"))
                            .setWidth(node.getInt("width"))
            ));
        });
    }

    private void extractImage(JSONObject obj, ParserResp resp) {
        Optional.ofNullable(obj.getByPath("images", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
            resp.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.IMAGE)
                            .setUrl(image.getByPath("url_list.0", String.class))
                            .setHeight(image.getInt("height"))
                            .setWidth(image.getInt("width"))
            );
            JSONObject video = image.getJSONObject("video");
            if (video != null) {
                resp.getMedias().add(
                        new ParserResp.Media()
                                .setType(MediaTypeEnum.LIVE)
                                .setUrl(video.getByPath("['play_addr']['url_list'][0]", String.class))
                                .setHeight(video.getInt("height"))
                                .setWidth(video.getInt("width"))
                );
            }
        }));
    }
}

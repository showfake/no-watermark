package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.dtflys.forest.http.ForestProxy;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.config.ProxyConfig;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.base.utils.JsonUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import com.lauzzl.nowatermark.factory.Parser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Slf4j
public class Threads implements Parser {

    @Resource
    private ProxyConfig proxyConfig;

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String shortId = UrlUtil.getLastPath(url);
        ForestProxy proxy = proxyConfig.proxy();
        HttpRequest request = HttpUtil.getHttpRequest(url, proxy);
        String response = request
                .setFollowRedirects(true)
                .execute().body();
        if (StrUtil.isBlank(response)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(response, shortId);
    }

    private Result<ParserResp> extract(String response, String shortId) {
        ParserResp result = new ParserResp();
        List<String> allGroups = ReUtil.getAllGroups(Pattern.compile("data-sjs>(.*?)</script"), response, false, true);
        for (String group : allGroups) {
            if (StrUtil.containsAll(group, shortId, "RelayPrefetchedStreamCache", "adp_BarcelonaPostPageDirectQueryRelayPreloader")) {
                JSONObject jsonData =(JSONObject) JsonUtil.extractKeyValue(group, "result");
                if (jsonData != null) {
                    extractInfo(jsonData, result);
                    extractVideo(jsonData, result);
                    extractImage(jsonData, result);
                }
            }
        }
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }

    private void extractInfo(JSONObject itemObject, ParserResp result) {
        result.setCover(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post['image_versions2'].candidates[0].url", String.class));
        result.setTitle(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post.caption.text", String.class));
        result.getAuthor()
                .setNickname(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post.user.username", String.class))
                .setAvatar(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post.user['profile_pic_url']", String.class));
    }

    private void extractVideo(JSONObject itemObject, ParserResp result) {
        Optional.ofNullable(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post['video_versions']", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(u -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(u.getStr("url"))
                    .setType(MediaTypeEnum.VIDEO));
        }));

    }

    private void extractImage(JSONObject itemObject, ParserResp result) {
        // gif
        //Optional.ofNullable(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post['giphy_media_info'].images", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
        Optional.ofNullable(itemObject.getByPath("data.data.edges[0].node['thread_items'][0].post['image_versions2'].candidates", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(image.getStr("url"))
                    .setHeight(image.getInt("height"))
                    .setWidth(image.getInt("width"))
                    .setType(MediaTypeEnum.IMAGE));
        }));
    }
}
package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.dtflys.forest.http.ForestProxy;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.config.ProxyConfig;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.JsonUtil;
import com.lauzzl.nowatermark.factory.Parser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Slf4j
public class Instagram extends Parser {

    @Resource
    private ProxyConfig proxyConfig;

    @Override
    public Result<ParserResp> execute() throws Exception {
        ForestProxy proxy = proxyConfig.proxy();
        HttpRequest request = HttpUtil.createGet(url);
        if (proxy != null) {
            request.setHttpProxy(proxy.getHost(), proxy.getPort());
            if (StrUtil.isAllNotBlank(proxy.getUsername(), proxy.getPassword())) {
                request.basicProxyAuth(proxy.getUsername(), proxy.getPassword());
            }
        }
        String response = request
                .setFollowRedirects(true)
                .execute().body();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(response);
    }

    private Result<ParserResp> extract(String response) {
        ParserResp result = new ParserResp();
        String shortId = getLastPath(url);
        List<String> allGroups = ReUtil.getAllGroups(Pattern.compile("data-sjs>(.*?)</script"), response, false, true);
        for (String group : allGroups) {
            if (StrUtil.containsAll(group, shortId, "RelayPrefetchedStreamCache", "adp_PolarisPostRootQueryRelayPreloader")) {
                JSONObject jsonData =(JSONObject) JsonUtil.extractKeyValue(group, "result");
                if (jsonData != null) {
                    extractInfo(jsonData, result);
                    extractVideo(jsonData, result);
                    extractImage(jsonData, result);
                }
            }
        }
        resetCover(result);
        return Result.success(result);
    }

    private void extractInfo(JSONObject itemObject, ParserResp result) {
        result.setCover(itemObject.getByPath("data['xdt_api__v1__media__shortcode__web_info'].items[0]['image_versions2'].candidates[0].url", String.class));
        result.setTitle(itemObject.getByPath("data['xdt_api__v1__media__shortcode__web_info'].items[0].caption.text", String.class));
        result.getAuthor()
                .setNickname(itemObject.getByPath("data['xdt_api__v1__media__shortcode__web_info'].items[0].user.username", String.class))
                .setAvatar(itemObject.getByPath("data['xdt_api__v1__media__shortcode__web_info'].items[0].user['profile_pic_url']", String.class));
    }

    private void extractVideo(JSONObject itemObject, ParserResp result) {
        Optional.ofNullable(itemObject.getByPath("extensions['all_video_dash_prefetch_representations'][0].representations", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(u -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(u.getStr("base_url"))
                    .setHeight(u.getInt("height"))
                    .setWidth(u.getInt("width"))
                    .setType(MediaTypeEnum.VIDEO));
        }));

    }

    private void extractImage(JSONObject itemObject, ParserResp result) {
        Optional.ofNullable(itemObject.getByPath("data['xdt_api__v1__media__shortcode__web_info'].items[0]['carousel_media']", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(image.getByPath("['image_versions2'].candidates[0].url", String.class))
                    .setHeight(image.getByPath("['image_versions2'].candidates[0].height", Integer.class))
                    .setWidth(image.getByPath("['image_versions2'].candidates[0].width", Integer.class))
                    .setType(MediaTypeEnum.IMAGE));
        }));
    }
}
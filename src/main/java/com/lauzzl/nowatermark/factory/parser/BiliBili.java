package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestCookie;
import com.dtflys.forest.http.ForestRequest;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class BiliBili extends Parser {

    @Value("${account.bilibili.cookie}")
    private String cookie;

    private static final String[] PATH_NAMES = new String[]{"video"};
    private static final String[] QUERY_NAMES = new String[]{"p"};

    private static final String BASE_URL = "https://api.bilibili.com/x";


    @Override
    public Result<ParserResp> execute() throws Exception {
        String redirectUrl = HttpUtil.getRedirectUrl(url, UserAgentPlatformEnum.DEFAULT);
        String bVid = getId(redirectUrl, PATH_NAMES, null);
        // 视频合集当前集数
        String p = getId(redirectUrl, null, QUERY_NAMES);
        if (StrUtil.isBlank(bVid)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        ForestRequest<?> request = Forest.get(BASE_URL + "/web-interface/view")
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .addQuery("bvid", bVid);
        if (StrUtil.isNotBlank(cookie)) {
            // 设置cookie(key:value;key:value;)
            Arrays.stream(cookie.split(";")).forEach(cookie -> {
                String[] split = cookie.split("=");
                if (split.length != 2) {
                    return;
                }
                request.addCookie(new ForestCookie(split[0], split[1]));
            });
        }
        String response = request.executeAsString();
        JSONObject jsonObject = JSONUtil.parseObj(response);
        String cid = getCid(jsonObject, p);
        if (StrUtil.isBlank(cid)) {
            log.error("获取cid失败：{}", response);
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        response = request.url(BASE_URL + "/player/wbi/playurl")
                .clearQueries()
                .addQuery("cid", cid)
                .addQuery("bvid", bVid)
                .addQuery("qn", 0)
                .addQuery("fnver", 0)
                .addQuery("fnval", 4048)
                .addQuery("fourk", 1)
                .addQuery("gaia_source", "")
                .addQuery("from_client", "BROWSER")
                .addHeader("Referer", url)
                .executeAsString();
        return extract(response, jsonObject, p);
    }

    private Result<ParserResp> extract(String content, JSONObject jsonObject, String p) {
        ParserResp result = new ParserResp();
        JSONObject playObj = JSONUtil.parseObj(content);
        int code = playObj.getInt("code");
        if (code != 0) {
            log.error("解析链接：{} 错误，返回结果：{}", url, content);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        extractVideo(playObj, result);
        extractAudio(playObj, result);
        extractInfo(jsonObject, result, p);
        return Result.success(result);
    }


    private void extractInfo(JSONObject jsonObject, ParserResp result, String p) {
        result.setTitle(jsonObject.getByPath("data.title", String.class));
        result.getAuthor()
                .setNickname(jsonObject.getByPath("data.owner.name", String.class))
                .setAvatar(jsonObject.getByPath("data.owner.face", String.class));
        String cover = StrUtil.isBlank(p) ? jsonObject.getByPath("data.pic", String.class) : jsonObject.getByPath("data.pages[" + (Integer.parseInt(p) - 1) + "].first_frame", String.class);
        result.setCover(cover);
    }

    private void extractVideo(JSONObject playObj, ParserResp result) {
        playObj.getByPath("data.dash.video", JSONArray.class).toList(JSONObject.class).forEach(video -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(video.getStr("baseUrl"))
                    .setType(MediaTypeEnum.VIDEO)
                    .setHeight(video.getInt("height"))
                    .setWidth(video.getInt("width"))
            );
        });
    }

    private void extractAudio(JSONObject playObj, ParserResp result) {
        playObj.getByPath("data.dash.audio", JSONArray.class).toList(JSONObject.class).forEach(audio -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(audio.getStr("baseUrl"))
                    .setType(MediaTypeEnum.AUDIO)
            );
        });
    }

    private String getCid(JSONObject jsonObject, String p) {
        String path = StrUtil.isBlank(p) ? "data.pages[0].cid" : "data.pages[" + (Integer.parseInt(p) - 1) + "].cid";
        return jsonObject.getByPath(path, String.class);
    }




}

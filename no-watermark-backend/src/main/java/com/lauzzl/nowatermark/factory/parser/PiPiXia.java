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
import com.lauzzl.nowatermark.base.utils.HttpUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.base.utils.UrlUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class PiPiXia implements Parser {

    private static final String BASE_URL = "https://api.pipix.com/bds/cell/cell_comment/?offset=0&cell_type=1&api_version=1&cell_id=%s&ac=wifi&channel=huawei_1319_64&aid=1319&app_name=super";

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        url = HttpUtil.getRedirectUrl(url, UserAgentPlatformEnum.DEFAULT);
        String id = UrlUtil.getId(url, "item", null);
        if (StrUtil.isBlank(id)) {
            return Result.failure(ErrorCode.PARSER_NOT_GET_ID);
        }
        String response = Forest.get(String.format(BASE_URL, id))
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.PHONE))
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_FAILED);
        }
        return extract(response);
    }


    private Result<ParserResp> extract(String content) {
        // 解析数据并设置
        // item -> data['cell_comments'][0]['comment_info'].item
        // title -> item.content
        // author -> item.author
        // nickname -> author.name
        // avatar -> author.avatar['url_list'][0].url
        // cover -> item.video.video_high.cover_image.url_list.0.url
        // video -> item.video.video_high
        // image -> item.note.multi_image
        ParserResp result = new ParserResp();
        JSONObject jsonObject = JSONUtil.parseObj(content);
        JSONObject itemObject = jsonObject.getByPath("data.cell_comments.0.comment_info.item", JSONObject.class);
        extractInfo(itemObject, result);
        extractVideo(itemObject, result);
        extractImage(itemObject, result);
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }


    /**
     * 提取帖子基本信息
     *
     * @param obj  obj
     * @param resp 返回结果
     */
    private void extractInfo(JSONObject obj, ParserResp resp) {
        resp.setTitle(obj.getStr("content"));
        resp.getAuthor()
                .setNickname(obj.getByPath("author.name", String.class))
                .setAvatar(obj.getByPath("author.avatar.url_list.0.url", String.class));
    }


    /**
     * 提取视频
     *
     * @param obj  obj
     * @param resp 返回结果
     */
    private void extractVideo(JSONObject obj, ParserResp resp) {
        resp.setCover(obj.getByPath("video.video_high.cover_image.url_list.0.url", String.class));
        Optional.ofNullable(obj.getByPath("video.video_high", JSONObject.class)).ifPresent(node -> {
            node.getJSONArray("url_list").toList(JSONObject.class).forEach(urlNode -> resp.getMedias().add(
                    new ParserResp.Media()
                            .setType(MediaTypeEnum.VIDEO)
                            .setUrl(urlNode.getStr("url"))
                            .setHeight(node.getInt("height"))
                            .setWidth(node.getInt("width"))
            ));
        });
    }


    /**
     * 提取图像
     *
     * @param obj  obj
     * @param resp 返回结果
     */
    private void extractImage(JSONObject obj, ParserResp resp) {
        Optional.ofNullable(obj.getByPath("note.multi_image", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(multi_image -> resp.getMedias().add(
                new ParserResp.Media()
                        .setType(MediaTypeEnum.IMAGE)
                        .setUrl(multi_image.getByPath("url_list.0.url", String.class))
                        .setHeight(multi_image.getInt("height"))
                        .setWidth(multi_image.getInt("width"))
        )));
    }

}

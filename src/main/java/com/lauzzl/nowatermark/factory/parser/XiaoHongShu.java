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
public class XiaoHongShu extends Parser {


    private String note_id;

    @Override
    public Result<ParserResp> execute() throws Exception {
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .executeAsString();
        if (StrUtil.isBlank(response)) {
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        response = ReUtil.get("window.__INITIAL_STATE__=(.*?)</script", response, 1);
        return extract(response.replace("undefined", "null"));
    }

    private Result<ParserResp> extract(String response) {
        JSONObject jsonObject = JSONUtil.parseObj(response);
        ParserResp result = new ParserResp();
        // noteData.data.noteData
        note_id = jsonObject.getByPath("note.firstNoteId", String.class);
        JSONObject noteData = jsonObject.getByPath("note", JSONObject.class);
        if (noteData == null || noteData.isEmpty() || StrUtil.isBlank(note_id)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        extractInfo(noteData, result);
        extractVideo(noteData, result);
        extractImage(noteData, result);
        resetCover(result);
        return Result.success(result);
    }

    private void extractImage(JSONObject noteData, ParserResp result) {
        Optional.ofNullable(noteData.getByPath("noteDetailMap['" + note_id + "'].note.imageList", JSONArray.class)).ifPresent(node -> node.toList(JSONObject.class).forEach(image -> {
            // 是否是实况
            boolean livePhoto = image.getBool("livePhoto");
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(livePhoto?image.getByPath("stream.h264[0].masterUrl", String.class):image.getStr("urlDefault"))
                    .setType(livePhoto?MediaTypeEnum.LIVE:MediaTypeEnum.IMAGE)
                    .setResolution(String.format("%sx%s", image.get("width"), image.get("height")))
            );
        }));
    }

    private void extractVideo(JSONObject noteData, ParserResp result) {
        Optional.ofNullable(noteData.getByPath("noteDetailMap['" + note_id + "'].note.video.media.stream", JSONObject.class)).ifPresent(node -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(node.getByPath("h264[0].masterUrl", String.class))
                    .setType(MediaTypeEnum.VIDEO)
                    .setResolution(String.format("%sx%s", node.getStr("h264[0].width"), node.getStr("h264[0].height")))
            );
        });
    }

    private void extractInfo(JSONObject noteData, ParserResp result) {
        String title = noteData.getByPath("noteDetailMap['" + note_id + "'].note.title") +
                "\n" +
                noteData.getByPath("noteDetailMap['" + note_id + "'].note.desc");
        result.setTitle(title);
        result.getAuthor()
                .setNickname(noteData.getByPath("noteDetailMap['" + note_id + "'].note.user.nickname", String.class))
                .setAvatar(noteData.getByPath("noteDetailMap['" + note_id + "'].note.user.avatar", String.class));
        Optional.ofNullable(noteData.getByPath("imageList[0].url", String.class)).ifPresent(result::setCover);
    }
}

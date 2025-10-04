package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.dtflys.forest.Forest;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
@Component
@Slf4j
public class WeChatMP implements Parser {
    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String response = Forest.get(url)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .executeAsString();
        if (!StrUtil.contains(response, "var title = ")) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_PARSE_MEDIA_INFO_FAILED);
        }
        return extract(response);
    }

    private Result<ParserResp> extract(String response) {
        ParserResp resp = new ParserResp();
        extractInfo(response, resp);
        extractImage(response, resp);
        ParserResultUtils.resetCover(resp);
        return Result.success(resp);
    }

    private void extractImage(String response, ParserResp resp) {
        ReUtil.findAll(Pattern.compile("width: '(.*?)' *[\\s|\\S]*?height: '(.*?)' *[\\s|\\S]*?cdn_url: '(.*?)'"), response, matcher -> {
            String width = NumberUtil.isNumber(matcher.group(1)) ? matcher.group(1) : null;
            String height = NumberUtil.isNumber(matcher.group(2)) ? matcher.group(2) : null;
            String url = matcher.group(3);
            if (!"0".equals(width)) {
                resp.getMedias().add(new ParserResp.Media()
                        .setUrl(url)
                        .setWidth(width == null ? null : Integer.parseInt(width))
                        .setHeight(height == null ? null : Integer.parseInt(height)));
            }
        });
    }

    private void extractInfo(String response, ParserResp resp) {
        resp.setTitle(ReUtil.get("var title = '(.*?)'", response, 1));
        resp.setCover(ReUtil.get("var cdn_url_235_1 = \"(.*?)\";", response, 1));
        resp.getAuthor()
                .setNickname(ReUtil.get("var author = \"(.*?)\";", response, 1))
                .setAvatar(ReUtil.get("var hd_head_img = \"(.*?)\"", response, 1));
    }
}

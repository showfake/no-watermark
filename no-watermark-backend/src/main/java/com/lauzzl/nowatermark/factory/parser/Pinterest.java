package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.config.ProxyConfig;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.ParserResultUtils;
import com.lauzzl.nowatermark.factory.Parser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
@Slf4j
public class Pinterest implements Parser {

    @Resource
    private ProxyConfig proxyConfig;

    @Override
    public Result<ParserResp> execute(String url) throws Exception {
        String result;
        ForestRequest<?> request = Forest.get(url)
                .setProxy(proxyConfig.proxy())
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT));
        ForestResponse response = request.execute(ForestResponse.class);
        result = response.readAsString();
        // forest对307 308重定向未做判断
        if (response.statusIs(308)) {
            result = request.url(response.getRedirectionLocation()).executeAsString();
        }
        String data = ReUtil.get("<script data-relay-response=\"true\" type=\"application/json\">(.*?)</script>", result, 1);
        if (StrUtil.isBlank(data)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, result);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(data);
    }

    private Result<ParserResp> extract(String response) {
        ParserResp result = new ParserResp();
        JSONObject jsonObject = JSONUtil.parseObj(response);
        extractInfo(jsonObject, result);
        extractImage(jsonObject, result);
        extractVideo(jsonObject, result);
        ParserResultUtils.resetCover(result);
        return Result.success(result);
    }

    private void extractVideo(JSONObject jsonObject, ParserResp result) {
        Optional.ofNullable(jsonObject.getByPath("response.data.v3GetPinQuery.data.storyPinData.pages[0].blocks[0].videoDataV2.videoList720P.v720P", JSONObject.class)).ifPresent(item -> {
            result.getMedias().add(new ParserResp.Media()
                    .setUrl(item.getStr("url"))
                    .setHeight(item.getInt("height"))
                    .setWidth(item.getInt("width"))
                    .setType(MediaTypeEnum.VIDEO));
        });
    }

    private void extractImage(JSONObject jsonObject, ParserResp result) {
        result.getMedias().add(new ParserResp.Media()
                .setUrl(jsonObject.getByPath("response.data.v3GetPinQuery.data.imageLargeUrl", String.class))
                .setType(MediaTypeEnum.IMAGE)
        );
    }

    private void extractInfo(JSONObject jsonObject, ParserResp result) {
        result.setTitle(
                jsonObject.getByPath("response.data.v3GetPinQuery.data.gridTitle", String.class) + "\n"
                        + jsonObject.getByPath("response.data.v3GetPinQuery.data.closeupUnifiedDescription", String.class)
        );
        result.getAuthor()
                .setNickname(jsonObject.getByPath("response.data.v3GetPinQuery.data.linkDomain.officialUser.username", String.class))
                .setAvatar(jsonObject.getByPath("response.data.v3GetPinQuery.data.linkDomain.officialUser.imageMediumUrl", String.class));
    }
}

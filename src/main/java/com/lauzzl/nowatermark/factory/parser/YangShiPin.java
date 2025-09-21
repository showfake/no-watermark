package com.lauzzl.nowatermark.factory.parser;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dtflys.forest.Forest;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.enums.UserAgentPlatformEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.base.utils.CommonUtil;
import com.lauzzl.nowatermark.base.utils.CryptoUtil;
import com.lauzzl.nowatermark.factory.Parser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
@Slf4j
public class YangShiPin extends Parser {


    private static final String GUID = "mft9t9id_9etjjlqngy";
    private static final String PLATFORM = "4330701";

    @Override
    public Result<ParserResp> execute() throws Exception {
        String realUrl = url;
        if (!realUrl.contains("vid")) {
            realUrl = ReUtil.get("URL='(.*?)'", Forest.get(realUrl).executeAsString(),1);
        }
        String response = Forest.get(realUrl)
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .executeAsString();
        if (StrUtil.isBlank(response) || !response.contains("__STATE_video__")) {
            log.error("解析链接：{} 获取视频信息失败，返回结果：{}", realUrl, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        return extract(response);
    }



    private Result<ParserResp> extract(String response) throws DecoderException {
        String data = ReUtil.get("__STATE_video__=(.*?)</script>", response, 1);
        if (StrUtil.isBlank(data)) {
            log.error("解析链接：{} 失败，返回结果：{}", url, response);
            return Result.failure(ErrorCode.PARSER_GET_POST_FAILED);
        }
        ParserResp resp = new ParserResp();
        JSONObject jsonObj = JSONUtil.parseObj(data);
        extractInfo(jsonObj, resp);
        extractVideo(jsonObj, resp);
        resetCover(resp);
        return Result.success(resp);
    }

    private void extractInfo(JSONObject jsonObj, ParserResp resp) {
        resp.setTitle(jsonObj.getByPath("payloads.sharevideo.title", String.class));
        resp.setCover(jsonObj.getByPath("payloads.sharevideo['cover_pic']", String.class));
    }

    private void extractVideo(JSONObject jsonObj, ParserResp resp) throws DecoderException {
        String vid = jsonObj.getByPath("payloads.sharevideo.vid", String.class);
        String cKey = getCKey(vid, GUID);
        String response = Forest.get("https://playvv.yangshipin.cn/playvinfo")
                .setUserAgent(CommonUtil.getUserAgent(UserAgentPlatformEnum.DEFAULT))
                .addQuery("guid", GUID)
                .addQuery("vid", vid)
                .addQuery("otype", "json")
                .addQuery("appVer", "1.43.0")
                .addQuery("encryptVer", "8.1")
                .addQuery("platform", PLATFORM)
                .addQuery("cKey", cKey)
                .addHeader("Referer", "https://www.yangshipin.cn/")
                .executeAsString();
        JSONObject jsonObject = JSONUtil.parseObj(response.substring(1).substring(0, response.length() - 1));
        if (jsonObject.isEmpty() || jsonObject.getInt("exem") != 2) {
            log.error("解析链接：{} 获取视频信息失败，返回结果：{}", url, response);
            return;
        }
        String baseUrl = jsonObject.getByPath("vl.vi[0].ul.ui[0].url", String.class);
        String path = jsonObject.getByPath("vl.vi[0].fn", String.class);
        String videoUrl = UrlBuilder.of(baseUrl + path)
                .addQuery("sdtfrom", PLATFORM)
                .addQuery("guid", GUID)
                .addQuery("vkey", jsonObject.getByPath("vl.vi[0].fvkey", String.class))
                .addQuery("platform", "2")
                .build();
        resp.getMedias().add(new ParserResp.Media()
                .setType(MediaTypeEnum.VIDEO)
                .setUrl(videoUrl)
                .setWidth(jsonObject.getByPath("vl.vi[0].vw", Integer.class))
                .setHeight(jsonObject.getByPath("vl.vi[0].vh", Integer.class))
        );
    }


    /**
     * 获取 cKey值
     *
     * @param vid  视频id
     * @param guid 访客uid，需要与请求中query.guid对应
     * @return {@link String }
     * @throws DecoderException 解码器异常
     */
    private String getCKey(String vid, String guid) throws DecoderException {
        String timeStr = String.valueOf(System.currentTimeMillis() / 1000).substring(0, 10);
        String data = String.format(
                "|%s|%s|mg3c3b04ba|1.43.0|%s|4330701|https://w.yangshipin.cn/|mozilla/5.0 (windows nt |https://m.yangshipin.cn/|Mozilla|Netscape|Win32|",
                vid, timeStr, guid
        );
        int o = 0;
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            o = (o << 5) - o + c;
            o &= o;
        }
        long qn = o;
        String encryptContent = "|" + qn + data;
        System.out.println(encryptContent);
        String encrypted = CryptoUtil.AESEncrypt(encryptContent, Hex.decodeHex("4E2918885FD98109869D14E0231A0BF4"), Hex.decodeHex("16B17E519DDD0CE5B79D7A63A4DD801C"), Mode.CBC, Padding.PKCS5Padding, "hex", Charset.defaultCharset());
        return "--01" + encrypted.toUpperCase();
    }

}

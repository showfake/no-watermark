package com.lauzzl.nowatermark.service;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.lauzzl.nowatermark.base.code.ErrorCode;
import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.model.req.ParserReq;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;
import com.lauzzl.nowatermark.factory.Parser;
import com.lauzzl.nowatermark.factory.ParserFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 解析服务
 *
 * @author LauZzL
 * @date 2025/08/30
 */
@Slf4j
@Service
public class ParserService {

    @Resource
    private ParserFactory parserFactory;

    /**
     * 执行
     *
     * @param req {@link ParserReq }
     * @return {@link Result }<{@link ParserResp }>
     */
    public Result<ParserResp> execute(ParserReq req) throws Exception {
        Parser parser = parserFactory.setUrl(extractUrl(req.getUrl())).build();
        if (parser == null) {
            return Result.failure(ErrorCode.PARSER_NOT_SUPPORT);
        }
        Result<ParserResp> result = parserFactory.build().execute();
        if (ObjectUtil.isEmpty(result.getData().getMedias())) {
            return Result.failure(ErrorCode.PARSER_NOT_FOUND_MEDIA);
        }
        return result;
    }


    /**
     * 提取 URL
     *
     * @param content 内容
     * @return {@link String }
     */
    private static String extractUrl(String content) {
        String url = ReUtil.getAllGroups(PatternPool.URL, content).get(0);
        if (StrUtil.isNotBlank(url) && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

}

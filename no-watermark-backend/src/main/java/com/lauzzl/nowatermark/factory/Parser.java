package com.lauzzl.nowatermark.factory;

import com.lauzzl.nowatermark.base.domain.Result;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;


/**
 * 解析 器
 *
 * @author LauZzL
 * @date 2025/08/29
 */
public interface Parser {

    Result<ParserResp> execute(String url) throws Exception;

}

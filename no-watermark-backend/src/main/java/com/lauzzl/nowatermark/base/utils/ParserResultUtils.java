package com.lauzzl.nowatermark.base.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lauzzl.nowatermark.factory.enums.MediaTypeEnum;
import com.lauzzl.nowatermark.base.model.resp.ParserResp;

import java.util.Optional;


/**
 * 解析器结果处理工具
 *
 * @author whisk
 * @date 2025/10/04
 */
public class ParserResultUtils {

    /**
     * 重置封面（若为空则使用第一张图片）
     * @param resp 解析结果
     */
    public static void resetCover(ParserResp resp) {
        Optional.ofNullable(resp).ifPresent(e -> {
            if (StrUtil.isBlank(e.getCover()) && ObjectUtil.isNotEmpty(e.getMedias())) {
                e.getMedias().stream()
                        .filter(m -> m.getType() == MediaTypeEnum.IMAGE)
                        .findFirst()
                        .ifPresent(m -> e.setCover(m.getUrl()));
            }
        });
    }
}
package com.lauzzl.nowatermark.base.enums;

import com.lauzzl.nowatermark.factory.Parser;
import com.lauzzl.nowatermark.factory.parser.*;
import lombok.Getter;

/**
 * 视频平台类型正则
 * @author LauZzL
 */
@Getter
public enum PlatformTypeRegexEnum {

    PPX("皮皮虾", ".*pipix.com.*", PiPiXia.class),
    DOUYIN("抖音", ".*v.douyin.com.*|.*www.douyin.com.*|.*www.iesdouyin.com.*", DouYin.class),
    KUAISHOU("快手", ".*www.kuaishou.com.*|.*v.kuaishou.com.*|.*v.m.chenzhongtech.com.*|.*gifshow.com.*", KuaiShou.class),
    WEISHI("微视", ".*video.weishi.qq.com.*|.*isee.weishi.qq.com.*", WeiShi.class),
    PIPIGAOXIAO("皮皮搞笑", ".*pipigx.com.*|.*ippzone.*", PiPiGaoXiao.class),
    ZUIYOU("最右", ".*xiaochuankeji.cn.*", ZuiYou.class),
    TIKTOK("Tik Tok", ".*tiktok.*", TikTok.class),
    TWITTER("推特", ".*x.com.*", Twitter.class),
    XIAOHONGSHU("小红书", ".*xiaohongshu.com.*|.*xhslink.*", XiaoHongShu.class),
    WEIBO("微博", ".*m.weibo.cn.*|.*weibo.com.*", WeiBo.class),
    ACFUN("AcFun", ".*www.acfun.cn.*", AcFun.class),
    INSTAGRAM("Instagram", ".*www.instagram.com.*", Instagram.class),
    JINRITOUTIAO("今日头条", ".*toutiao.com.*", JinRiTouTiao.class),
    ;

    private final String platformName;
    private final String regex;
    private final Class<? extends Parser> parserClass;

    PlatformTypeRegexEnum(String platformName, String regex, Class<? extends Parser> parser) {
        this.regex = regex;
        this.platformName = platformName;
        this.parserClass = parser;
    }

}

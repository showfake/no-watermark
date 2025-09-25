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

    PPX("皮皮虾", ".*pipix.com.*", "https://iili.io/KlEZJUu.png", PiPiXia.class),
    DOUYIN("抖音", ".*v.douyin.com.*|.*www.douyin.com.*|.*www.iesdouyin.com.*", "https://iili.io/KlEZ4zQ.png", DouYin.class),
    KUAISHOU("快手", ".*www.kuaishou.com.*|.*v.kuaishou.com.*|.*v.m.chenzhongtech.com.*|.*gifshow.com.*", "https://iili.io/KlEtcTx.png", KuaiShou.class),
    WEISHI("微视", ".*video.weishi.qq.com.*|.*isee.weishi.qq.com.*", "https://iili.io/KlED1AG.png", WeiShi.class),
    PIPIGAOXIAO("皮皮搞笑", ".*pipigx.com.*|.*ippzone.*", "https://iili.io/KlEVT0u.png", PiPiGaoXiao.class),
    ZUIYOU("最右", ".*xiaochuankeji.cn.*", "https://iili.io/KlEXz8b.md.png", ZuiYou.class),
    TIKTOK("Tik Tok", ".*tiktok.*", "https://iili.io/KlEpUxf.png", TikTok.class),
    TWITTER("推特", ".*x.com.*", "https://iili.io/KlEeHc7.png", Twitter.class),
    XIAOHONGSHU("小红书", ".*xiaohongshu.com.*|.*xhslink.*", "https://iili.io/KlEkD42.md.png", XiaoHongShu.class),
    WEIBO("微博", ".*m.weibo.cn.*|.*weibo.com.*", "https://iili.io/KlGH8Dg.png", WeiBo.class),
    ACFUN("AcFun", ".*www.acfun.cn.*", "https://iili.io/KlG9Nun.png", AcFun.class),
    INSTAGRAM("Instagram", ".*www.instagram.com.*", "https://iili.io/KlGJlqv.png", Instagram.class),
    JINRITOUTIAO("今日头条", ".*toutiao.com.*", "https://iili.io/KlGdR3l.png", JinRiTouTiao.class),
    BILIBILI("哔哩哔哩", ".*www.bilibili.com.*", "https://iili.io/KlG2alR.png", BiliBili.class),
    YANGSHIPIN("央视频", ".*w.yangshipin.cn.*|.*www.yspapp.cn.*", "https://iili.io/KlGKXig.md.png", YangShiPin.class),
    ;

    private final String platformName;
    private final String regex;
    private final String logo;
    private final Class<? extends Parser> parserClass;

    PlatformTypeRegexEnum(String platformName, String regex, String logo ,Class<? extends Parser> parser) {
        this.regex = regex;
        this.platformName = platformName;
        this.logo = logo;
        this.parserClass = parser;
    }

}

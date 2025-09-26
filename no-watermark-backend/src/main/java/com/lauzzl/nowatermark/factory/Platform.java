package com.lauzzl.nowatermark.factory;

import com.lauzzl.nowatermark.factory.parser.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Platform {

    private static final List<Platform> ALL_PLATFORMS = new ArrayList<>();

    public static final Platform PiPiXia = new Platform(
            "皮皮虾",
            ".*pipix.com.*",
            "https://iili.io/KlEZJUu.png",
            PiPiXia.class
    );

    public static final Platform DouYin = new Platform(
            "抖音",
            ".*v.douyin.com.*|.*www.douyin.com.*|.*www.iesdouyin.com.*",
            "https://iili.io/KlEZ4zQ.png",
            DouYin.class
    );

    public static final Platform KuaiShou = new Platform(
            "快手",
            ".*www.kuaishou.com.*|.*v.kuaishou.com.*|.*v.m.chenzhongtech.com.*|.*gifshow.com.*",
            "https://iili.io/KlEtcTx.png",
            KuaiShou.class
    );

    public static final Platform WeiShi = new Platform(
            "微视",
            ".*video.weishi.qq.com.*|.*isee.weishi.qq.com.*",
            "https://iili.io/KlED1AG.png",
            WeiShi.class
    );

    public static final Platform PiPiGaoXiao = new Platform(
            "皮皮搞笑",
            ".*pipigx.com.*|.*ippzone.*",
            "https://iili.io/KlEVT0u.png",
            PiPiGaoXiao.class
    );

    public static final Platform ZuiYou = new Platform(
            "最右",
            ".*xiaochuankeji.cn.*",
            "https://iili.io/KlEXz8b.md.png",
            ZuiYou.class
    );

    public static final Platform TikTok = new Platform(
            "Tik Tok",
            ".*tiktok.*",
            "https://iili.io/KlEpUxf.png",
            TikTok.class
    );

    public static final Platform Twitter = new Platform(
            "推特",
            ".*x.com.*",
            "https://iili.io/KlEeHc7.png",
            Twitter.class
    );

    public static final Platform XiaoHongShu = new Platform(
            "小红书",
            ".*xiaohongshu.com.*|.*xhslink.*",
            "https://iili.io/KlEkD42.md.png",
            XiaoHongShu.class
    );

    public static final Platform WeiBo = new Platform(
            "微博",
            ".*m.weibo.cn.*|.*weibo.com.*",
            "https://iili.io/KlGH8Dg.png",
            WeiBo.class
    );

    public static final Platform AcFun = new Platform(
            "AcFun",
            ".*www.acfun.cn.*",
            "https://iili.io/KlG9Nun.png",
            AcFun.class
    );

    public static final Platform instagram = new Platform(
            "Instagram",
            ".*www.instagram.com.*",
            "https://iili.io/KlGJlqv.png",
            Instagram.class
    );

    public static final Platform JinRiTouTiao = new Platform(
            "今日头条",
            ".*toutiao.com.*",
            "https://iili.io/KlGdR3l.png",
            JinRiTouTiao.class
    );

    public static final Platform BiliBili = new Platform(
            "哔哩哔哩",
            ".*www.bilibili.com.*",
            "https://iili.io/KlG2alR.png",
            BiliBili.class
    );

    public static final Platform YangShiPin = new Platform(
            "央视频",
            ".*w.yangshipin.cn.*|.*www.yspapp.cn.*",
            "https://iili.io/KlGKXig.md.png",
            YangShiPin.class
    );


    public static final Platform Threads = new Platform(
            "Threads",
            ".*www.threads.com.*",
            "https://iili.io/K0Ye7Ie.png",
            Threads.class
    );

    public static final Platform Pinterest = new Platform(
            "Pinterest",
            ".*www.pinterest.com.*|.*pin.it.*",
            "https://iili.io/K01AAn1.png",
            Pinterest.class
    );


    private final String platformName;
    private final String regex;
    private final String logo;
    private final Class<? extends Parser> parserClass;


    private Platform(String platformName, String regex, String logo, Class<? extends Parser> parserClass) {
        this.platformName = platformName;
        this.regex = regex;
        this.logo = logo;
        this.parserClass = parserClass;
        ALL_PLATFORMS.add(this);
    }

    public static List<Platform> getAllPlatforms() {
        return new ArrayList<>(ALL_PLATFORMS);
    }
}
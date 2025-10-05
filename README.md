![no-watermark](https://socialify.git.ci/LauZzL/no-watermark/image?custom_description=%E5%9F%BA%E4%BA%8E+Spring+Boot+3+%E7%9A%84%E7%9F%AD%E8%A7%86%E9%A2%91%2F%E5%9B%BE%E9%9B%86%E5%8E%BB%E6%B0%B4%E5%8D%B0%E6%9C%8D%E5%8A%A1&custom_language=Spring+Boot&description=1&font=Inter&forks=1&language=1&name=1&owner=1&pattern=Circuit+Board&stargazers=1&theme=Auto)

<p align="center">
<p align="center">
<a href="#支持平台">🎯 支持平台</a> |
<a href="#构建">🛠️ 构建</a> |
<a href="#账号配置">⚙️ 账号配置</a> |
<a href="#代理配置">🌐 代理配置</a> |
<a href="#缓存配置">💾 缓存配置</a> |
<a href="#参与开发">👥 参与开发</a> |
<a href="#常见问题">❓️ 常见问题</a> |
<a href="#小程序在线体验">📱 小程序体验</a>
</p>



## 项目介绍

**一个基于 Spring Boot 3 的短视频/图集/实况去水印服务**

![frontend](/screenshot/shots.png)



## 支持平台

| 平台       | 支持域名                                            | 视频 | 图集 | 实况 |
| ---------- | --------------------------------------------------- | ---- | ---- | ---- |
| 推特       | `x.com` `twitter.com`                               | ✅    | ✅    | -    |
| 皮皮虾     | `h5.pipix.com`                                      | ✅    | ✅    | -    |
| 抖音       | `v.douyin.com` `www.douyin.com` `www.iesdouyin.com` | ✅    | ✅    | ✅    |
| Tik Tok    | `vm.tiktok.com` `www.tiktok.com`                    | ✅    | ❌️    | ❌️    |
| 最右       | `share.xiaochuankeji.cn`                            | ✅    | ✅    | -    |
| 皮皮搞笑   | `h5.ippzone.com`                                    | ✅    | ✅    | -    |
| 微视       | `v.weishi.qq.com` `video.weishi.qq.com`             | ✅    | ✅    | -    |
| 小红书     | `xhslink.com` `www.xiaohongshu.com`                 | ✅    | ✅    | ✅    |
| 快手       | `www.kuaishou.com`                                  | ✅    | ✅    | -    |
| 微博       | `weibo.com` `m.weibo.cn`                            | ✅    | ✅    | -    |
| AcFun      | `www.acfun.cn`                                      | ✅    | -    | -    |
| Instagram  | `www.instagram.com`                                 | ✅    | ✅    | -    |
| 今日头条   | `www.toutiao.com`  `m.toutiao.com`                  | ✅    | -    | -    |
| 哔哩哔哩   | `www.bilibili.com`                                  | ✅    | -    | -    |
| 央视频     | `w.yangshipin.cn`  `www.yspapp.cn`                  | ✅    | -    | -    |
| Threads    | `www.threads.com`                                   | ✅    | ✅    | -    |
| Pinterest  | `www.pinterest.com` `pin.it`                        | ✅    | ✅    | -    |
| Weverse    | `weverse.io`                                        | -    | ✅    | -    |
| Vimeo      | `vimeo.com`                                         | ✅    | ✅    | -    |
| 微信公众号 | `mp.weixin.qq.com`                                  | ✅    | ✅    | -    |
| 待添加     | -                                                   | -    | -    | -    |

## 账号配置

部分平台解析时需要配置账号信息。

### 推特

推特解析需要在 `application-${spring.profiles.active}.yml` 文件中配置 Twitter 账号信息。

<details>
<summary>获取推特账号信息</summary>

1. 首先需要在浏览器登录Twitter账号。
2. `F12` 或 `Ctrl+Shift+I` 打开开发者工具，选择`Network`标签。
3. `F5` 或 `Ctrl+R` 刷新页面。
4. 在`Network`标签中，`Ctrl+F` 搜索 `live_pipeline/update_subscriptions`。
5. 点击 `live_pipeline/update_subscriptions`，在 `Request Headers` 标签，找到 `x-csrf-token`、`Cookie`、`Authorization` 参数，复制上方yaml中需要的参数值并将其填入配置文件中。

![](/screenshot/tw-1.png)

</details>


```yaml
# 平台账号设置
account:
  # 推特账号设置
  twitter:
    # cookie.auth_token
    auth_token: 必填
    # header.x-csrf-token
    x_csrf_token: 必填
    # header.authorization
    authorization: 必填
```

### 哔哩哔哩

哔哩哔哩设置账号cookie后可获取最高分辨率视频。

```yaml
account:
  bilibili:
    cookie: "你的B站Cookie"  # 登录B站后获取的Cookie
```

哔哩哔哩视频解析时，如果视频是合集，复制链接时请确保url.query中含有p值，否则无法正常获取当前选中的集数。

```
// p=9该视频合集的第9集
eg: https://www.bilibili.com/video/xxxxxx?spm_id_from=333.788.videopod.episodes&vd_source=xxxx&p=9
```


## 代理配置

在解析部分平台时需要国外网络环境，需要在 `application-${spring.profiles.active}.yml` 中配置代理信息。

```yaml
forest:
  proxy:
    # 启用后仅在请求国外站点时才启用代理(本机是国外网络环境则不需要启用)
    enable: true
    # http or socks (docs:https://forest.kim/pages/1.7.x/api_proxy/)
    type: http
    host: 127.0.0.1
    port: 7890
    # username:
    # password:
```

## 缓存配置

部分平台可能需要使用redis来缓存一些数据，需要 `application-${spring.profiles.active}.yml` 文件中配置redis连接信息。

```yaml
spring:
  data:
    redis:
      # Redis数据库索引（默认为0）
      database: 0
      # Redis服务器地址
      host: 127.0.0.1
      # Redis服务器连接端口
      port: 6379
      # Redis服务器连接密码（默认为空）
      password:
      # 连接超时时间
      timeout: 10s
      lettuce:
        pool:
          # 连接池最大连接数
          max-active: 200
          # 连接池最大阻塞等待时间（使用负值表示没有限制）
          max-wait: -1ms
          # 连接池中的最大空闲连接
          max-idle: 10
          # 连接池中的最小空闲连接
          min-idle: 0
```



## 构建

### 前端

> ⚠️ 注意：React 19 版本中 Semi Design Toast 组件存在报错问题（详见 Semi Design Issue [#2980](https://github.com/DouyinFE/semi-design/issues/2980)），建议使用 React 18 版本开发。

```shell
cd no-watermark-frontend
# 安装依赖
yarn install
# 启动开发环境
yarn dev
```

### 后端

项目启动成功后可以访问 `http://localhost:10010/doc.html` 查看Api文档。

```shell
cd no-watermark-backend
# 安装依赖
./mvnw clean install
# 运行
./mvnw spring-boot:run -D spring-boot.run.jvmArguments="-Dspring.profiles.active=dev"
```



## 参与开发

加入该项目同开发者共同维护。

- 你可以通过 [PR](https://github.com/LauZzL/no-watermark/pulls) 对项目代码做出贡献
- 你可以通过 [Issues](https://github.com/LauZzL/no-watermark/issues) 提交问题或提出建议

## 常见问题

### **哔哩哔哩视频下载403**

哔哩哔哩获取的视频格式是m4s，播放或下载时需要添加Referer参数，否则会触发防盗链。

```
referer: https://www.bilibili.com/
```

### **微信公众号视频下载403**

微信公众号视频在下载时需要补齐header信息，在下载时需要携带下方请求头。

```
# 断点续传
range: bytes=0-
host: mpvideo.qpic.cn
origin: https://mp.weixin.qq.com
referer: https://mp.weixin.qq.com/
```




## 小程序在线体验

<div align="center" >
<img style="display: block; margin: 0 auto; " src="https://iili.io/FtOBlkX.jpg" width="200" height="200" />
</div>


## 免责声明

本项目仅供学习交流，请勿用于非法用途。
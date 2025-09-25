import { Toast, VideoPlayer, Space, Button } from "@douyinfe/semi-ui";
import { IconPlay } from "@douyinfe/semi-icons";
import type { MediaInfo } from "../types/types";
import { useState } from "react";

export const Media = ({ data }: { data: MediaInfo }) => {
  //   data = {
  //     title: "#涞觅猎手连体衣",
  //     author: {
  //       nickname: "大大大Kit",
  //       avatar:
  //         "https://p11.douyinpic.com/aweme/100x100/aweme-avatar/tos-cn-avt-0015_3b6b202848df0ad5dbde8378e26978fb.jpeg?from=327834062",
  //     },
  //     cover:
  //       "https://p3-sign.douyinpic.com/tos-cn-i-0813c000-ce/oUHxA0qQiuMJAinCbAeEAwEIEVAWEGB7AICfer~tplv-dy-resize-walign-adapt-aq:540:q75.webp?lk3s=138a59ce&x-expires=1759924800&x-signature=sda6d6O%2BVReuvK8lWaD7ASwe3Lw%3D&from=327834062&s=PackSourceEnum_DOUYIN_REFLOW&se=false&sc=cover&biz_tag=aweme_video&l=20250924200453CF89A50C254F03CC4C2E",
  //     medias: [
  //       {
  //         type: "VIDEO",
  //         url: "https://v5-ali-east.douyinvod.com/5145bf68b001d3eab16a23662593af9d/68d3f654/video/tos/cn/tos-cn-ve-15c000-ce/osUQIHliGIeDaxeeAFLg4s2XDFeyiDTQIzA6Dx/?a=1128&ch=0&cr=0&dr=0&cd=0%7C0%7C0%7C0&cv=1&br=949&bt=949&cs=0&ds=3&ft=LjVHEX998xIOuE0mO0P58lZW_3iXfBqRRVJEFXRwJ0PD-Ipz&mime_type=video_mp4&qs=0&rc=OTgzNzNlNWVkZDtoNzs1Z0BpajQzcXk5cmxrNTMzbGkzNEBjMmBeM2A1X18xNC0vLy0uYSNqMjY1MmRzc2xhLS1kLWJzcw%3D%3D&btag=80010e00088000&cquery=100y&dy_q=1758718006&feature_id=fea919893f650a8c49286568590446ef&l=20250924204646A2BEC55115DC00D4445F",
  //         height: 1920,
  //         width: 1080,
  //       },
  //     ],
  //   };

  const [playUrl, setPlayUrl] = useState<string | null>(null);

  const copyText = (text: string) => {
    navigator.clipboard.writeText(text);
    Toast.success("复制成功");
  };

  const getBtnText = (media: MediaInfo["medias"][number], index: number) => {
    let text = "";
    switch (media.type) {
      case "VIDEO":
        text = "视频";
        break;
      case "AUDIO":
        text = "音频";
        break;
      case "IMAGE":
        text = "图片";
        break;
      case "LIVE":
        text = "实况";
        break;
    }
    const resolution =
      media.height && media.width ? `(${media.width}p x ${media.height}p)` : "";
    return text + (index + 1) + resolution;
  };

  return (
    <div className="flex w-full flex-col md:mt-8 md:mb-8">
      <div className="flex gap-2 mb-4 flex-col">
        {data?.author && (
          <div className="flex items-center gap-2">
            {data?.author.avatar && (
              <img
                src={data.author.avatar}
                className="w-10 h-10 rounded-full"
              />
            )}
            {data?.author.nickname && (
              <div className="flex text-4 font-bold">
                {data.author.nickname}
              </div>
            )}
          </div>
        )}
        {data?.title && (
          <span
            onClick={() => {
              copyText(data.title);
            }}
            className="text-gray contents break-words cursor-pointer"
          >
            {data.title}
          </span>
        )}
      </div>
      <Space className="mb-4" wrap>
        {data?.cover && (
          <Button onClick={() => window.open(data.cover)}>封面</Button>
        )}
        {data?.medias.map((item, index) => {
          return (
            <Button
              icon={<IconPlay />}
              key={index}
              onClick={() => {
                setPlayUrl(item.url);
              }}
            >
              {getBtnText(item, index)}
            </Button>
          );
        })}
      </Space>
      {data?.medias && playUrl && (
        <VideoPlayer
          src={playUrl}
          poster={data.cover}
          crossOrigin="anonymous"
          width={"100%"}
        ></VideoPlayer>
      )}
    </div>
  );
};

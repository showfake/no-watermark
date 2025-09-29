import { Toast, Space, Button } from "@douyinfe/semi-ui";
import { IconPlay } from "@douyinfe/semi-icons";
import type { MediaInfo } from "../types/types";

export const Media = ({ data }: { data: MediaInfo }) => {

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
              onClick={()=>window.open(item.url)}
            >
              {getBtnText(item, index)}
            </Button>
          );
        })}
      </Space>
    </div>
  );
};

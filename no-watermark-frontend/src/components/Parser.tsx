import { useState } from "react";
import { useMount, useRequest } from "ahooks";
import { Button, Input, Toast, Image } from "@douyinfe/semi-ui";
import { IconSend, IconGlobeStroke } from "@douyinfe/semi-icons";
import { api } from "../apis/index";
import { Media } from "./Media";
import type { Platform } from "../types/types";

export const Parser = () => {
  const [url, setUrl] = useState("");
  const [platforms, setPlatforms] = useState<Platform[] | null>(null);

  useMount(async () => {
    try {
      const res = await api.support();
    if (res.code !== 0) {
      Toast.error(`获取支持平台失败：${res}`);
      return;
    }
    setPlatforms(res.data);
    } catch (error) {
      console.error('获取支持平台失败', error)
      Toast.error(`获取支持平台失败：${error}`);
    }
  });

  const { data, loading, error, run } = useRequest(api.parser, {
    manual: true,
    onSuccess(data) {
      if (data.code !== 0) {
        Toast.error(`解析失败：${data.msg}`);
        return;
      }
    },
  });

  if (error) {
    console.error('解析出现异常', error)
    Toast.error(`异常：${error.message}`);
  }

  return (
    <div className="flex items-center flex-col w-full overflow-x-hidden">
      <div className="flex w-80vw xl:w-40vw h-[fit-content]">
        <Input
          value={url}
          autoComplete="off"
          className="flex-[1]"
          prefix={<IconGlobeStroke />}
          onChange={(value) => setUrl(value)}
          size="large"
          placeholder="请输入要解析的作品链接"
          showClear
        ></Input>
        <Button
          icon={<IconSend />}
          size="large"
          onClick={() => run(url)}
          loading={loading}
        >
          解析
        </Button>
      </div>
      {data?.data && (
        <div className="flex w-80vw xl:w-[40vw] mt-[40px] xl:mt-0">
          <Media data={data.data} />
        </div>
      )}
      {platforms && (
        <div className="text-[12px] mt-[20px] flex flex-col w-80vw xl:w-40vw">
          <h2>支持平台</h2>
          <div className="grid grid-cols-4 xl:grid-cols-8 w-full p-1">
            {platforms.map((item) => (
              <div key={item.name} className="flex items-center justify-center p-1">
                <Image
                  className="rounded-[8px]"
                  width={64}
                  height={64}
                  src={item.logo}
                  alt={item.name}
                />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

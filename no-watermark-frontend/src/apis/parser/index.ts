import request from "../request";
import type { MediaInfo, Data } from "../../types/types";

export default async function parser(url: string): Promise<Data<MediaInfo>> {
  return await request("/parser/executor", {
    url
  });
};
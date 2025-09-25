import request from "../request";
import type { Platform, Data } from "../../types/types";

export default async function support(): Promise<Data<Platform[]>> {
  return await request("/support/all");
};
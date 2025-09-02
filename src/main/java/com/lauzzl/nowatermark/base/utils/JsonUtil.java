package com.lauzzl.nowatermark.base.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class JsonUtil {

    /**
     * 从JSON数据中提取指定键的值
     * @param jsonStr JSON字符串或JSON对象
     * @param keyToFind 要查找的键
     * @return 找到的键对应的值，如果未找到则返回null
     */
    public static Object extractKeyValue(String jsonStr, String keyToFind) {
        Object jsonData;
        if (JSONUtil.isTypeJSONObject(jsonStr)) {
            jsonData = JSONUtil.parseObj(jsonStr);
        } else if (JSONUtil.isTypeJSONArray(jsonStr)) {
            jsonData = JSONUtil.parseArray(jsonStr);
        } else {
            return null;
        }

        return findKey(jsonData, keyToFind);
    }

    /**
     * 递归查找指定键的值
     * @param jsonData JSON对象、JSON数组或其他值
     * @param key 要查找的键
     * @return 找到的键对应的值，如果未找到则返回null
     */
    private static Object findKey(Object jsonData, String key) {
        if (jsonData instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) jsonData;
            if (jsonObj.containsKey(key)) {
                return jsonObj.get(key);
            }
            // 递归检查所有子元素
            for (String k : jsonObj.keySet()) {
                Object value = jsonObj.get(k);
                Object result = findKey(value, key);
                if (result != null) {
                    return result;
                }
            }
        }
        // 处理JSONArray类型
        else if (jsonData instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonData;
            // 遍历数组中的每个元素
            for (Object item : jsonArray) {
                Object result = findKey(item, key);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


}

package com.lauzzl.nowatermark.base.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.Charset;

/**
 * 加密工具类
 *
 * @author whisk
 * @date 2025/09/21
 */
public class CryptoUtil {


    /**
     * AES
     *
     * @param key     key
     * @param iv      iv
     * @param mode    模式
     * @param padding 填充
     * @return {@link String }
     */
    public static AES Aes(byte[] key, byte[] iv, Mode mode, Padding padding) {
        return new AES(mode, padding, key, iv);
    }

    /**
     * HMAC
     *
     * @param key           钥匙
     * @param hmacAlgorithm HMAC算法
     * @return {@link String }
     */
    public static HMac Hmac(byte[] key, HmacAlgorithm hmacAlgorithm) {
        return DigestUtil.hmac(hmacAlgorithm, key);
    }



}

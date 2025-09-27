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
     * aes加密
     *
     * @param data    明文
     * @param key     key
     * @param iv      iv
     * @param mode    模式
     * @param padding 填充
     * @param output  输出(hex/base64)
     * @param charset 字符集
     * @return {@link String }
     */
    public static String AESEncrypt(String data, byte[] key, byte[] iv, Mode  mode, Padding padding, String output, Charset charset) {
        AES aes = new AES(mode, padding, key, iv);
        if (output.equals("hex")) {
            return aes.encryptHex(data, charset);
        } else {
            return aes.encryptBase64(data, charset);
        }
    }

    /**
     * HMAC
     *
     * @param data          数据
     * @param key           钥匙
     * @param hmacAlgorithm HMAC算法
     * @param output        输出(hex/base64)
     * @param charset       字符集
     * @return {@link String }
     */
    public static String Hmac(String data, byte[] key, HmacAlgorithm hmacAlgorithm, String output, Charset charset) {
        HMac hmac = DigestUtil.hmac(hmacAlgorithm, key);
        if (output.equals("hex")) {
            return hmac.digestHex(data, charset);
        } else {
            return hmac.digestBase64(data, false);
        }
    }



}

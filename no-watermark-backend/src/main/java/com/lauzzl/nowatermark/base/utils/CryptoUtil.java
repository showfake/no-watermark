package com.lauzzl.nowatermark.base.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
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
     * 艾森地穴
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

}

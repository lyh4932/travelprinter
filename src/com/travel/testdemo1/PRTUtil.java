package com.travel.testdemo1;

import java.io.UnsupportedEncodingException;

/**
 * 普瑞特打印工具类
 * @author linhx
 * @since 1.0
 */
public class PRTUtil {

    static {
        System.loadLibrary("printer");
    }

    public native static byte[] covert_print_data(byte[] content, int length);

    public static byte[] getPrintBytes(String text) throws UnsupportedEncodingException {
        byte[] content = text.getBytes("GBK");
        return covert_print_data(content,content.length);
    }
}

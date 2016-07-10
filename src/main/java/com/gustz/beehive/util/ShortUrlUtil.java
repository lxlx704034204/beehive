package com.gustz.beehive.util;

/**
 *
 */
public abstract class ShortUrlUtil {

    // private static final char[] cs = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final char[] cs = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final int radix = cs.length;

    // 将数字转换为62进制
    public static String to62(long number, int minLen) {
        StringBuilder buf = new StringBuilder();
        long num = number;
        while (num >= radix) {
            buf.append(cs[(int) (num % radix)]);
            num = num / radix;
        }
        buf.append(cs[(int) num]);
        for (int i = buf.length(); i < minLen; i++) {
            buf.append('0');
        }
        buf.reverse();
        return buf.toString();
    }

    // 将生成的key转换为10进制数字
    public static long from62(String s) {
        long ret = 0;
        boolean flag = true;
        for (char c : s.toCharArray()) {
            if (c == '0' && flag)
                continue;
            ret = ret * radix + sixtyTowValue(c);
            flag = false;
        }
        return ret;
    }

    private static int sixtyTowValue(char c) {
        if (c >= '0' && c <= '9')
            return c - '0';
        else if (c >= 'a' && c <= 'z')
            return c - 'a' + 10;
        else if (c >= 'A' && c <= 'Z')
            return c - 'A' + 36;
        else
            throw new IllegalArgumentException("Char not allowed here: " + c);
    }
}

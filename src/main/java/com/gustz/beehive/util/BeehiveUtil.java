package com.gustz.beehive.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.UUID;

/**
 * util
 *
 * @author zhangzhenfeng
 * @since 2016-10-20
 */
public abstract class BeehiveUtil {

    public static int getCurrTimestamp() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateSpUUID() {
        return generateUUID().replaceAll("-", "");
    }

    static String fmtYmd() {
        return FastDateFormat.getInstance("yyyy-MM-dd").format(new Date());
    }

    public static String fmtYmdHms() {
        return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String fmtChinaYmd(int timestamp) {
        long ts = ((long) timestamp) * 1000;
        return FastDateFormat.getInstance("yyyy年MM月dd日").format(ts);
    }

    public static String fmtChinaMd(int timestamp) {
        long ts = ((long) timestamp) * 1000;
        return FastDateFormat.getInstance("MM月dd日").format(ts);
    }

    public static String fmtYmd(int timestamp) {
        long ts = ((long) timestamp) * 1000;
        return FastDateFormat.getInstance("yyyy-MM-dd").format(ts);
    }

    public static String fmtYmdHms(int timestamp) {
        long ts = ((long) timestamp) * 1000;
        return FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(ts);
    }

}

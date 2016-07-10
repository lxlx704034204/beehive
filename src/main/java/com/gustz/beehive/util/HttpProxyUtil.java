package com.gustz.beehive.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * XXX
 *
 * @author zhangzhenfeng
 * @since 2016-04-07
 */
public class HttpProxyUtil {

    private static String proxyHost = "www.baidu.com";

    private static int proxyPort = 8080;

    private static String proxyUser = "user";

    private static String proxyPass = "pass";

    public static String doProxy(String url) throws IOException {
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", "" + proxyPort);
        //
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", "" + proxyPort);
        InputStream is = null;
        try {
            URLConnection conn = new URL(url).openConnection();
            HttpsURLConnection httpsCon = (HttpsURLConnection) conn;
            httpsCon.setFollowRedirects(true);
            //
            String encoding = conn.getContentEncoding();
            if (encoding == null || encoding.isEmpty()) {
                encoding = "UTF-8";
            }
            is = conn.getInputStream();
            return toString(is, encoding);
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static String toString(InputStream input, String encoding)
            throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }


    public static void copy(InputStream input, Writer output, String encoding)
            throws IOException {
        if (encoding == null) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    public static void copy(InputStream input, Writer output)
            throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[4098];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}

package com.zl.aliyun.iot;

import com.zl.aliyun.util.StringUtil;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/02  17:43
 */
public class UrlUtil {
    private final static String CHARSET_UTF8 = "utf8";

    public static String urlEncode(String url) {
        if (!StringUtil.isEmpty(url)) {
            try {
                url = URLEncoder.encode(url, "UTF-8");
            } catch (Exception e) {
                System.out.println("Url encode error:" + e.getMessage());
            }
        }
        return url;
    }

    public static String generateQueryString(Map<String, String> params, boolean isEncodeKV) {
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (isEncodeKV) {
                canonicalizedQueryString.append(percentEncode(entry.getKey())).append("=")
                    .append(percentEncode(entry.getValue())).append("&");
            } else { canonicalizedQueryString.append(entry.getKey()).append("=").append(entry.getValue()).append("&"); }
        }
        if (canonicalizedQueryString.length() > 1) {
            canonicalizedQueryString.setLength(canonicalizedQueryString.length() - 1);
        }
        return canonicalizedQueryString.toString();
    }

    public static String percentEncode(String value) {
        try {
            return value == null ? null : URLEncoder.encode(value, CHARSET_UTF8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception e) {

        }
        return "";
    }
}

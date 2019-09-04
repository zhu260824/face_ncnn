package com.zl.aliyun.util;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/02  18:00
 */
public class UrlUtil {
    private final static String URL_ENCODING = "UTF-8";

    /**
     * URL编码 使用UTF-8字符集按照 RFC3986 规则编码请求参数和参数取值
     */
    public static String percentEncode(String value) {
        try {
            return value != null ? URLEncoder.encode(value, URL_ENCODING).replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~") : null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String generateQueryString(Map<String, String> params, boolean isEncode) {
        if (null == params || params.size() < 1) {return null;}
        String[] sortedKeys = params.keySet().toArray(new String[] {});
        Arrays.sort(sortedKeys);
        StringBuilder queryBuilder = new StringBuilder();
        for (String key : sortedKeys) {
            if (isEncode) {
                queryBuilder.append(percentEncode(key)).append("=")
                    .append(percentEncode(params.get(key))).append("&");
            } else {
                queryBuilder.append(key).append("=")
                    .append(params.get(key)).append("&");
            }
        }
        if (queryBuilder.length() > 1) {
            queryBuilder.setLength(queryBuilder.length() - 1);
        }
        return queryBuilder.toString();
    }

    public static String urlEncode(String url) {
        if (!StringUtil.isEmpty(url)) {
            try {
                url = URLEncoder.encode(url, URL_ENCODING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return url;
    }
}

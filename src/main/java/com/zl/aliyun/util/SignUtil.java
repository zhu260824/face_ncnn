package com.zl.aliyun.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/03  10:24
 */
public class SignUtil {
    private final static String CHARSET_UTF8 = "utf8";
    private final static String SEPARATOR = "&";
    private static final String ENCODING = "UTF-8";

    /**
     * 获取UUID
     */
    public static String getUniqueNonce() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String generateHmacSHA1SignString(String method, String urlPath, Map<String, String> params) {
        String queryString = UrlUtil.generateQueryString(params, true);
        return generateHmacSHA1SignString(method, urlPath, queryString);
    }

    public static String generateHmacSHA1SignString(String method, String urlPath, String queryString) {
        StringBuilder strBuilderSign = new StringBuilder();
        strBuilderSign.append(method);
        strBuilderSign.append(SEPARATOR);
        strBuilderSign.append(UrlUtil.percentEncode(urlPath));
        strBuilderSign.append(SEPARATOR);
        strBuilderSign.append(UrlUtil.percentEncode(queryString));
        return strBuilderSign.toString();
    }

    public static byte[] hmacSHA1Signature(String secret, String baseString) throws Exception {
        if (StringUtil.isEmpty(secret)) {
            throw new IOException("secret can not be empty");
        }
        if (StringUtil.isEmpty(baseString)) {
            return null;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), ENCODING);
        mac.init(keySpec);
        return mac.doFinal(baseString.getBytes(CHARSET_UTF8));
    }

    public static String hmacSHA1Sign(String secret, String baseString) {
        try {
            byte[] signData = hmacSHA1Signature(secret, baseString);
            if (signData != null && signData.length > 0) {
                return Base64Encoder.encode(signData);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

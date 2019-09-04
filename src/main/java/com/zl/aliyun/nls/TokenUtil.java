package com.zl.aliyun.nls;

import com.alibaba.fastjson.TypeReference;
import com.zl.aliyun.util.DateUtil;
import com.zl.aliyun.util.SignUtil;
import com.zl.aliyun.util.StringUtil;
import com.zl.aliyun.util.UrlUtil;
import com.zl.aliyun.util.http.HttpCallBack;
import com.zl.aliyun.util.http.HttpUtil;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/03  10:47
 */
public class TokenUtil {
    private static final String BASE_URL = "http://nls-meta.cn-shanghai.aliyuncs.com";
    private static TokenUtil INSTANCE;
    private static WeakReference<NlsToken> tokenReference;

    public TokenUtil() {
    }

    public static String getToken(String accessKeyId, String accessKeySecret) {
        String token = getMemberToken();
        if (StringUtil.isEmpty(token)) {
            String queryString = generateTokenQuery(accessKeyId, accessKeySecret);
            NlsToken nlsToken = synQueryToken(queryString);
            if (nlsToken != null && !StringUtil.isEmpty(nlsToken.getId())) {
                return nlsToken.getId();
            } else {
                return null;
            }
        } else {
            return token;
        }
    }

    public static void getToken(String accessKeyId, String accessKeySecret, TokenListener tokenListener) {
        String token = getMemberToken();
        if (StringUtil.isEmpty(token)) {
            String queryString = generateTokenQuery(accessKeyId, accessKeySecret);
            aynQueryToken(queryString, tokenListener);
        } else {
            if (tokenListener != null) {
                tokenListener.onSuccess(token);
            }
        }
    }

    private static String getMemberToken() {
        if (null == tokenReference) {return null;}
        NlsToken nlsToken = tokenReference.get();
        if (null == nlsToken || StringUtil.isEmpty(nlsToken.getId())) {return null;}
        Date tokenDate = new Date(nlsToken.getExpireTime() * 1000);
        Date nowDate = new Date(System.currentTimeMillis());
        if (tokenDate.after(nowDate)) {
            return nlsToken.getId();
        } else {
            return null;
        }
    }

    private static void saveToken(NlsToken nlsToken) {
        if (nlsToken != null) {
            tokenReference = new WeakReference<>(nlsToken);
        }
    }

    public static String generateTokenQuery(String accessKeyId, String accessKeySecret) {
        Map<String, String> queryParamsMap = new HashMap<>();
        queryParamsMap.put("AccessKeyId", accessKeyId);
        queryParamsMap.put("Action", "CreateToken");
        queryParamsMap.put("Version", "2019-02-28");
        queryParamsMap.put("Timestamp", DateUtil.getISO8601Time(null));
        queryParamsMap.put("Format", "JSON");
        queryParamsMap.put("RegionId", "cn-shanghai");
        queryParamsMap.put("SignatureMethod", "HMAC-SHA1");
        queryParamsMap.put("SignatureVersion", "1.0");
        queryParamsMap.put("SignatureNonce", SignUtil.getUniqueNonce());
        String queryString = UrlUtil.generateQueryString(queryParamsMap, true);
        //System.out.println("规范化后的请求参数串：" + queryString);
        if (null == queryString) {
            //System.out.println("构造规范化的请求字符串失败！");
            return null;
        }
        String signString = SignUtil.generateHmacSHA1SignString("GET", "/", queryString);
        //System.out.println("构造的签名字符串：" + signString);
        if (null == signString) {
            //System.out.println("构造签名字符串失败");
            return null;
        }
        String signature = SignUtil.hmacSHA1Sign(accessKeySecret + "&", signString);
        //System.out.println("计算的得到的签名：" + signature);
        if (null == signature) {
            //System.out.println("计算签名失败!");
            return null;
        }
        /**
         * 4.将签名加入到第1步获取的请求字符串
         */
        String queryStringWithSign = "Signature=" + signature + "&" + queryString;
        //System.out.println("带有签名的请求字符串：" + queryStringWithSign);
        return queryStringWithSign;
    }

    public static NlsToken synQueryToken(String queryString) {
        String url = BASE_URL + "/" + "?" + queryString;
        HashMap<String, String> headers = new HashMap<>(1);
        headers.put("Accept", "application/json");
        NlsTokenResponse tokenResponse = HttpUtil.doGet(url, null, headers, new TypeReference<NlsTokenResponse>() {});
        if (tokenResponse != null && tokenResponse.getToken() != null) {
            saveToken(tokenResponse.getToken());
            return tokenResponse.getToken();
        } else {
            return null;
        }
    }

    public static void aynQueryToken(String queryString, TokenListener tokenListener) {
        String url = BASE_URL + "/" + "?" + queryString;
        HashMap<String, String> headers = new HashMap<>(1);
        headers.put("Accept", "application/json");
        HttpUtil.doGet(url, null, headers, new HttpCallBack<NlsTokenResponse>() {
            @Override
            public void onSuccess(NlsTokenResponse data) {
                if (data != null && data.getToken() != null) {
                    saveToken(data.getToken());
                    if (tokenListener != null) {
                        tokenListener.onSuccess(data.getToken().getId());
                    }
                }
            }

            @Override
            public void onException(Exception e) {
                if (tokenListener != null) {
                    tokenListener.onError(e);
                }
            }
        });
    }

}

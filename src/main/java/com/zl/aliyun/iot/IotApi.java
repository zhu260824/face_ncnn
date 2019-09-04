package com.zl.aliyun.iot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/02  17:40
 */
public class IotApi {
    // AccessKey 信息
    public static String accessKey = "1234567890123456";
    public static String accessKeySecret = "123456789012345678901234567890";

    public final static String CHARSET_UTF8 = "utf8";

    /**
     * @param args
     * @throws UnsupportedEncodingException
     * @1. 需求修改Config.java中的AccessKey信息
     * @2. 建议使用方法二，所有参数都需要一一填写
     * @3. "最终signature"才是你需要的签名最终结果
     */
    public static void main(String[] args) throws UnsupportedEncodingException {

        // 方法一
        System.out.println("方法一：");
        String str = "GET&%2F&AccessKeyId%3D" + accessKey
            + "%26Action%3DRegisterDevice%26DeviceName%3D1533023037%26Format%3DJSON%26ProductKey%3DaxxxUtgaRLB%26RegionId%3Dcn-shanghai%26SignatureMethod%3DHMAC-SHA1%26SignatureNonce%3D1533023037%26SignatureVersion%3D1"
            + ".0%26Timestamp%3D2018-07-31T07%253A43%253A57Z%26Version%3D2018-01-20";
        byte[] signBytes;
        try {
            signBytes = SignatureUtils.hmacSHA1Signature(accessKeySecret + "&", str.toString());
            String signature = SignatureUtils.newStringByBase64(signBytes);
            System.out.println("signString---" + str);
            System.out.println("signature----" + signature);
            System.out.println("最终signature：" + URLEncoder.encode(signature, CHARSET_UTF8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();

        // 方法二
        System.out.println("方法二：");
        Map<String, String> map = new HashMap<String, String>();
        // 公共参数
        map.put("Format", "JSON");
        map.put("Version", "2018-01-20");
        map.put("AccessKeyId", accessKey);
        map.put("SignatureMethod", "HMAC-SHA1");
        map.put("Timestamp", "2018-07-31T07:43:57Z");
        map.put("SignatureVersion", "1.0");
        map.put("SignatureNonce", "1533023037");
        map.put("RegionId", "cn-shanghai");
        // 请求参数
        map.put("Action", "RegisterDevice");
        map.put("DeviceName", "1533023037");
        map.put("ProductKey", "axxxUtgaRLB");
        try {
            String signature = SignatureUtils.generate("GET", map, accessKeySecret);
            System.out.println("最终signature：" + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }
}

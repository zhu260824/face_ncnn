package com.zl.aliyun.util.http;

import com.alibaba.fastjson.TypeReference;
import com.zl.aliyun.util.JsonUtil;
import com.zl.aliyun.util.StringUtil;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zl
 * @Version 1.0
 * @Description TODO
 * @date 2019/01/16  14:52
 */
public class HttpUtil {
    private static long TIMEOUT_WRITE = 20;
    private static long TIMEOUT_READ = 20;
    private static long TIMEOUT_CONNECT = 20;

    private volatile static OkHttpClient mClient;

    public static OkHttpClient getClient() {
        if (null == mClient) {
            synchronized (OkHttpClient.class) {
                if (null == mClient) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS);
                    builder.readTimeout(TIMEOUT_READ, TimeUnit.SECONDS);
                    builder.connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS);
                    mClient = builder.build();
                }
            }
        }
        return mClient;
    }

    public static void setTimeoutWrite(long timeoutWrite) {
        TIMEOUT_WRITE = timeoutWrite;
    }

    public static void setTimeoutRead(long timeoutRead) {
        TIMEOUT_READ = timeoutRead;
    }

    public static void setTimeoutConnect(long timeoutConnect) {
        TIMEOUT_CONNECT = timeoutConnect;
    }

    public static void doGet(String url, Map<String, String> params, Map<String, String> headers, HttpCallBack callBack) {
        String reqUrl = url;
        if (params != null && params.size() > 0) { reqUrl = reqUrl + "?" + parseUrlRequest(params); }
        Request.Builder builder = new Request.Builder();
        builder.url(reqUrl);
        if (headers != null && headers.size() > 0) { builder.headers(getHeaders(headers)); }
        request(builder.build(), callBack);
    }

    public static <T> T doGet(String url, Map<String, String> params, Map<String, String> headers, TypeReference<T> type) {
        String reqUrl = url;
        if (params != null && params.size() > 0) { reqUrl = reqUrl + "?" + parseUrlRequest(params); }
        Request.Builder builder = new Request.Builder();
        builder.url(reqUrl);
        if (headers != null && headers.size() > 0) { builder.headers(getHeaders(headers)); }
        return request(builder.build(), type);
    }

    public static void doPost(String url, Map<String, String> params, HttpCallBack callBack) {
        requestPost(url, getBody(params), null, callBack);
    }

    public static void doPostFile(String url, Map<String, String> params, FileContentFile file, HttpCallBack callBack) {
        requestPost(url, getBody(params, file), null, callBack);
    }

    public static void doPostFile(String url, Map<String, String> params, FileContentFile file, Map<String, String> heardes, HttpCallBack callBack) {
        requestPost(url, getBody(params, file), heardes, callBack);
    }

    public static <T> T doPost(String url, Map<String, String> params, TypeReference<T> type) {
        return requestPost(url, getBody(params), null, type);
    }

    public static <T> T doPostJson(String url, Map<String, ?> params, TypeReference<T> type) {
        Map<String, String> heardes = new HashMap<>(1);
        heardes.put("content-type", "application/json;charset:utf-8");
        return requestPost(url, getBodyForJson(params), heardes, type);
    }

    public static <T> T doPostFile(String url, Map<String, String> params, ByteContentFile file, TypeReference<T> type) {
        return requestPost(url, getBody(params, file), null, type);
    }

    public static void doPostFile(String url, Map<String, String> params, ByteContentFile file, HttpCallBack callBack) {
        requestPost(url, getBody(params, file), null, callBack);
    }

    public static <T> T doPostFile(String url, Map<String, String> params, FileContentFile file, TypeReference<T> type) {
        return requestPost(url, getBody(params, file), null, type);
    }

    public static <T> T doPostFile(String url, Map<String, String> params, FileContentFile file, Map<String, String> heardes, TypeReference<T> type) {
        return requestPost(url, getBody(params, file), heardes, type);
    }

    public static void requestPost(String url, RequestBody body, Map<String, String> headers, HttpCallBack callBack) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (headers != null && headers.size() > 0) { builder.headers(getHeaders(headers)); }
        builder.post(body);
        request(builder.build(), callBack);
    }

    public static <T> T requestPost(String url, RequestBody body, Map<String, String> headers, TypeReference<T> type) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (headers != null && headers.size() > 0) { builder.headers(getHeaders(headers)); }
        builder.post(body);
        return request(builder.build(), type);
    }

    public static void request(Request request, final HttpCallBack callBack) {
        getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callBack != null) { callBack.onException(e); }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (!StringUtil.isEmpty(result)) {
                    if (callBack != null) {
                        try {
                            Type type = ((ParameterizedType)callBack.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                            callBack.onSuccess(JsonUtil.fromJson(result, type));
                        } catch (Exception e) {
                            callBack.onException(e);
                        }
                    }
                }
            }
        });
    }

    public static <T> T request(Request request, TypeReference<T> type) {
        try {
            Response response = getClient().newCall(request).execute();
            String result = response.body().string();
            System.out.println(result);
            if (!StringUtil.isEmpty(result)) {
                return JsonUtil.fromJson(result, type);
            }
        } catch (
            IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Headers getHeaders(Map<String, ?> headers) {
        Headers.Builder builder = new Headers.Builder();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, ?> entry : headers.entrySet()) {
                builder.add(entry.getKey(), urlEncoderUTF8(entry.getValue().toString()));
            }
        }
        return builder.build();
    }

    public static RequestBody getBody(Map<String, ?> params) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                bodyBuilder.add(entry.getKey(), urlEncoderUTF8(entry.getValue().toString()));
            }
        }
        return bodyBuilder.build();
    }

    public static RequestBody getBodyForJson(Map<String, ?> params) {
        String json = JsonUtil.toJson(params);
        MediaType mediaType = MediaType.parse("application/json,charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json);
        return requestBody;
    }

    public static RequestBody getBody(Map<String, ?> params, ByteContentFile file) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                bodyBuilder.addFormDataPart(entry.getKey(), urlEncoderUTF8(entry.getValue().toString()));
            }
        }
        if (file != null && file.getContent() != null) {
            bodyBuilder.addFormDataPart(file.getReqName(), file.getFileName(), RequestBody.create(MediaType.parse("application/octet-stream"), file.getContent()));
        }
        return bodyBuilder.build();
    }

    public static RequestBody getBody(Map<String, ?> params, FileContentFile file) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                bodyBuilder.addFormDataPart(entry.getKey(), urlEncoderUTF8(entry.getValue().toString()));
            }
        }
        if (file != null && file.getFile() != null) {
            bodyBuilder.addFormDataPart(file.getReqName(), file.getFileName(), RequestBody.create(MediaType.parse("application/octet-stream"), file.getFile()));
        }
        return bodyBuilder.build();
    }

    public static RequestBody getBody(Map<String, ?> params, List<FileContentFile> fileList) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                bodyBuilder.addFormDataPart(entry.getKey(), urlEncoderUTF8(entry.getValue().toString()));
            }
        }
        if (fileList != null && fileList.size() > 0) {
            for (FileContentFile fileContentFile : fileList) {
                if (fileContentFile != null && fileContentFile.getFile() != null) {
                    bodyBuilder.addFormDataPart(fileContentFile.getReqName(), fileContentFile.getFileName(), RequestBody.create(MediaType.parse("application/octet-stream"), fileContentFile.getFile()));
                }
            }
        }
        return bodyBuilder.build();
    }

    public static String parseUrlRequest(Map<String, ?> params) {
        StringBuffer buffer = new StringBuffer();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                if (buffer.length() > 0) { buffer.append("&"); }
                buffer.append(String.format("%s=%s", urlEncoderUTF8(entry.getKey()), urlEncoderUTF8(entry.getValue().toString())));
            }
        }
        return buffer.toString();
    }

    public static String urlEncoderUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }
}

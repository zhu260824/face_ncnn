package com.zl.aliyun.util.http;

/**
 * @author zl
 * @Version 1.0
 * @Description TODO
 * @date 2019/01/16  14:51
 */
public interface HttpCallBack<T> {
    /**
     * 网络请求成功
     *
     * @param data
     * @return
     */
    void onSuccess(T data);

    /**
     * 网络请求异常
     *
     * @param e
     * @return
     */
    void onException(Exception e);
}

package com.zl.aliyun.nls;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/04  16:18
 */
public interface TokenListener {
    /**
     * 获取token成功
     *
     * @param token
     * @return
     */
    void onSuccess(String token);

    /**
     * 获取token失败
     *
     * @param e
     * @return
     */
    void onError(Exception e);
}

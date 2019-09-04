package com.zl.aliyun.nls;

import java.io.Serializable;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/03  16:19
 */
public class NlsToken implements Serializable {
    private static final long serialVersionUID = -841117051816861042L;
    private long ExpireTime;
    private String Id;
    private String UserId;

    public NlsToken() {
    }

    public long getExpireTime() {
        return ExpireTime;
    }

    public void setExpireTime(long expireTime) {
        ExpireTime = expireTime;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    @Override
    public String toString() {
        return "NlsToken{" +
            "ExpireTime=" + ExpireTime +
            ", Id='" + Id + '\'' +
            ", UserId='" + UserId + '\'' +
            '}';
    }
}

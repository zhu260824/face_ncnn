package com.zl.aliyun.nls;

import java.io.Serializable;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/03  16:18
 */
public class NlsTokenResponse implements Serializable {
    private static final long serialVersionUID = 4564566096568781967L;
    private String NlsRequestId;
    private String RequestId;
    private NlsToken Token;

    public NlsTokenResponse() {
    }

    public String getNlsRequestId() {
        return NlsRequestId;
    }

    public void setNlsRequestId(String nlsRequestId) {
        NlsRequestId = nlsRequestId;
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }

    public NlsToken getToken() {
        return Token;
    }

    public void setToken(NlsToken token) {
        Token = token;
    }

    @Override
    public String toString() {
        return "NlsTokenResponse{" +
            "NlsRequestId='" + NlsRequestId + '\'' +
            ", RequestId='" + RequestId + '\'' +
            ", Token=" + Token +
            '}';
    }
}

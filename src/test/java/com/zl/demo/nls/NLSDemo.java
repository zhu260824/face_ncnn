package com.zl.demo.nls;

import com.zl.aliyun.nls.TokenListener;
import com.zl.aliyun.nls.TokenUtil;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/03  16:26
 */
public class NLSDemo {
    private static final String accessKeyId = "LTAIQDnXhioZ6TJF";
    private static final String accessKeySecret = "njZGVbnrPph1dvW5ykMYgdFDO3WOBA";

    @Test
    public void testAsync() {
        TokenUtil.getToken(accessKeyId, accessKeySecret, new TokenListener() {
            @Override
            public void onSuccess(String token) {
                System.out.println("token:"+token);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("Exception:"+e);
            }
        });
        String token=  TokenUtil.getToken(accessKeyId, accessKeySecret);
        System.out.println("token:"+token);
    }
    @Test
    public void testSync() {
      String token=  TokenUtil.getToken(accessKeyId, accessKeySecret);
      System.out.println("token:"+token);
    }

    @Test
    public void testExpireTime(){
        long time=1567670678L;
        Date date=new Date(time*1000);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(date));
    }
}

package com.zl.aliyun.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/02  17:49
 */
public class DateUtil {
    private final static String TIME_ZONE = "GMT";
    private final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * 获取时间戳
     * 符合ISO8601规范，并需要使用UTC时间，时区为+0
     */
    public static String getISO8601Time(Date date) {
        Date nowDate = date;
        if (null == date) {
            nowDate = new Date();
        }
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.format(nowDate);
    }
}

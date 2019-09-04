package com.zl.aliyun.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description JSON解析工具
 * @date 2019/09/02  11:21
 */

public class JsonUtil {
    /**
     * @param object 类的实例
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        if (null == object) {
            return null;
        }
        return JSON.toJSONString(object);
    }

    /**
     * @param <T>   泛型声明
     * @param json  JSON字符串
     * @param clazz 要转换对象的类型
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (null == json || json.length() <= 0) { return null; }
        return JSON.parseObject(json, clazz);
    }

    public static <T> T fromJson(String json, TypeReference<T> type) {
        if (null == json || json.length() <= 0) { return null; }
        return JSON.parseObject(json, type);
    }

    public static <T> T fromJson(String json, Type type) {
        if (null == json || json.length() <= 0) { return null; }
        return JSON.parseObject(json, type);
    }

    /**
     * 解析json使用泛型转换为对应List
     *
     * @return
     */
    public static <T> List<T> json2List(String json, Class<T> clazz) {
        if (null == json || json.length() <= 0) { return null; }
        return JSON.parseArray(json, clazz);
    }

}

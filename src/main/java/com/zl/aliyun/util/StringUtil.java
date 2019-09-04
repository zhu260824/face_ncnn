package com.zl.aliyun.util;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZL @朱林</a>
 * @Version 1.0
 * @Description TODO
 * @date 2019/09/02  17:41
 */
public class StringUtil {

    public static boolean isEmpty(String value) {
        if (value != null && value.length() > 0) {
            return false;
        }
        return true;
    }

    public static List<String> stringToSList(String data) {
        return stringToSList(data, ",");
    }

    public static List<String> stringToSList(String data, String regex) {
        if (isEmpty(data) || isEmpty(regex)) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(data.split(regex)));
    }

    public static List<Long> stringToLList(String data) {
        return stringToLList(data, ",");
    }

    public static List<Long> stringToLList(String data, String regex) {
        if (isEmpty(data) || isEmpty(regex)) {
            return null;
        }
        String[] strings = data.split(regex);
        List<Long> list = new ArrayList<>();
        for (String string : strings) {
            Long values = stringToLong(string);
            if (values != null) {
                list.add(values);
            }
        }
        return list;
    }

    public static Integer stringToInteger(String data) {
        Integer value = null;
        try {
            if (!isEmpty(data)) {
                value = Integer.parseInt(data);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Long stringToLong(String data) {
        Long value = null;
        try {
            if (!isEmpty(data)) {
                value = Long.parseLong(data);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Double stringToDouble(String data) {
        Double value = null;
        try {
            if (!isEmpty(data)) {
                value = Double.parseDouble(data);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String IntegerToString(Integer data) {
        if (data != null) {
            return String.valueOf(data);
        } else {
            return "";
        }
    }

    public static String doubleToString(Double data) {
        if (data == null) { return ""; }

        return doubleToString(data, "%.2f");
    }

    public static String doubleToString(Double data, String format) {
        String value = null;
        if (data != null) {
            try {
                value = String.format(format, data);
            } catch (IllegalFormatException ex) {
                ex.printStackTrace();
            }
        }
        if (isEmpty(value)) {
            value = "0";
        }
        return value;
    }

    public static Boolean isNumeric(String str) {
        if (null == str || str.length() == 0) {
            return false;
        }
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getRandNumberString() {
        int min = 10000;
        int max = 99999;
        Random random = new Random();
        int randNumber = random.nextInt(max) % (max - min + 1) + min;
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        sb.append(randNumber);
        return sb.toString();
    }

    public static <T> String join(List<T> list, String split) {
        if (null == list || list.size() == 0 || null == split) {
            return null;
        }
        String strResult = "";
        StringBuilder stringBuilder = new StringBuilder();
        int splitLength = split.length();
        for (T tmp : list) {
            stringBuilder.append(tmp);
            stringBuilder.append(split);
        }
        strResult = stringBuilder.toString();
        strResult = strResult.substring(0, strResult.length() - splitLength);
        return strResult;
    }

    /**
     * 字符串中批量替换某些字符
     *
     * @param replacementDefine 要替换的定义，key为被替换掉的值，支持正则表达式；value为替换之后的值(value为$等正则字符时会报错)
     * @param string            要检索替换的字符串主体
     * @return 替换之后的字符串
     * @throws IllegalStateException     If no match has yet been attempted, or if the previous match operation failed
     * @throws IllegalArgumentException  If the replacement string refers to a named-capturing group that does not exist in the pattern
     * @throws IndexOutOfBoundsException If the replacement string refers to a capturing group that does not exist in the pattern
     */
    public static String batchReplace(Map<String, String> replacementDefine, String string) {
        if (null == replacementDefine || replacementDefine.size() == 0 || null == string || string.length() == 0) {
            return "";
        }
        Set<String> defineKetSet = replacementDefine.keySet();
        String regex = "(" + StringUtil.join(new ArrayList<>(defineKetSet), ")|(") + ")";
        Map<Integer, String> replaceMap = new HashMap<>();
        int keyCount = 1;
        for (String key : defineKetSet) {
            replaceMap.put(keyCount++, replacementDefine.get(key));
        }
        return batchReplaceInString(regex, replaceMap, string);
    }

    /**
     * 内部使用替换核心，专服务于batchReplace，filterBadWord
     *
     * @param regex      替换的正则表达式
     * @param replaceMap 被替换的单元定义
     * @param string
     * @return
     */
    private static String batchReplaceInString(String regex, Map<Integer, String> replaceMap, String string) {
        Set<Integer> replaceKeys = replaceMap.keySet();
        Matcher matcher = Pattern.compile(regex).matcher(string);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            for (Integer intKey : replaceKeys) {
                if (null != matcher.group(intKey)) {
                    matcher.appendReplacement(sb, replaceMap.get(intKey));
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String getUrlStringValue(String name, String value) {
        try {
            return new String(value.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUrlStringValue(String value) {
        return getUrlStringValue("文本", value);
    }

    public static List<Long> ids(String ids) {
        String[] sids = ids.split(",");
        List<Long> list = new ArrayList<Long>();
        for (String sid : sids) {
            try {
                list.add(Long.valueOf(sid));
            } catch (NumberFormatException ignored) {

            }
        }
        HashSet<Long> hs = new HashSet<Long>(list);
        list.clear();
        for (Long h : hs) {
            list.add(h);
        }
        return list;
    }
}

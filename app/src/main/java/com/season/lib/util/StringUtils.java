package com.season.lib.util;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isSpace(String s) {
        return s == null || s.trim().length() == 0;
    }
    public static String getHostName(String urlString) {
        String head = "";
        int index = urlString.indexOf("://");
        if (index != -1) {
            head = urlString.substring(0, index + 3);
            urlString = urlString.substring(index + 3);
        }
        index = urlString.indexOf("/");
        if (index != -1) {
            urlString = urlString.substring(0, index + 1);
        }
        return head + urlString;
    }

    public static String getDataSize(long var0) {
        DecimalFormat var2 = new DecimalFormat("###.00");
        return var0 < 1024L ? var0 + "bytes" : (var0 < 1048576L ? var2.format((double) ((float) var0 / 1024.0F)) + "KB" : (var0 <
                1073741824L ? var2.format((double) ((float) var0 / 1024.0F / 1024.0F)) + "MB" : (var0 < 0L ? var2.format((double) (
                        (float) var0 / 1024.0F / 1024.0F / 1024.0F)) + "GB" : "error")));
    }

    /**
     * 高亮 关键字
     *
     * @return
     */
    public final static SpannableStringBuilder markKeywordLight(String keyword, String targetStr, String color) {
        if (TextUtils.isEmpty(keyword)) {
            return new SpannableStringBuilder(TextUtils.isEmpty(targetStr) ? "" : targetStr);
        }

        int index = targetStr.indexOf(keyword);
        SpannableStringBuilder result = new SpannableStringBuilder(targetStr);
        if (index != -1) {
            result.setSpan(new ForegroundColorSpan(Color.parseColor(color)), index, index + keyword.length(), Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    /**
     * @param list
     * @param splite 以什么字符分割
     * @return
     * @说明 将list数组地址转化为urls地址字符串
     */
    public final static String converListToString(List<String> list, String splite) {
        String urlLink = "";
        if (list == null || list.size() == 0) return urlLink;
        for (String url : list) {
            urlLink += url + splite;
        }
        urlLink = urlLink.substring(0, urlLink.length() - 1);
        return urlLink;
    }

    /**
     * @param list
     * @param splite 以什么字符分割
     * @return
     * @说明 将list数组地址转化为urls地址字符串
     */
    public final static String converIntListToString(List<Integer> list, String splite) {
        String urlLink = "";
        if (list == null || list.size() == 0) return urlLink;
        for (int url : list) {
            urlLink += url + splite;
        }
        urlLink = urlLink.substring(0, urlLink.length() - 1);
        return urlLink;
    }

    /**
     * @param
     * @param splitExpressione
     * @return
     * @说明 将str数组字符串转为list
     */
    public final static List<String> converStringToList(String strStr,

                                                        String splitExpressione) {
        List<String> list = new ArrayList<String>();
        if (TextUtils.isEmpty(strStr)) {
            return list;
        }
        String[] array = strStr.split(splitExpressione);
        for (String str : array) {
            list.add(str);
        }
        return list;
    }

    /**
     * unicode 转字符串
     */
    public static String unicode2String(String unicode) {

        StringBuffer string = new StringBuffer();

        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) {

            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);

            // 追加成string
            string.append((char) data);
        }

        return string.toString();
    }

    public static boolean isNumber(String input) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(input);
        if (m.matches()) {
            return true;
        }
        return false;
    }

    public static boolean isLetter(String input) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(input);
        if (m.matches()) {
            return true;
        }
        return false;
    }
    public static boolean isPunctuation(String input) {
        Pattern p = Pattern.compile("[，。.]");
        Matcher m = p.matcher(input);
        if (m.matches()) {
            return true;
        }
        return false;
    }

    public static boolean isChinese(String input) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(input);
        if (m.matches()) {
            return true;
        }
        return false;
    }

}

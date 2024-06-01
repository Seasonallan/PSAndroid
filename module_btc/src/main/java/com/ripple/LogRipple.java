package com.ripple;

import android.util.Log;

import com.season.btc.BuildConfig;

public class LogRipple {

    static boolean enable = BuildConfig.DEBUG;

    public static void enableLog(boolean log) {
        enable = log;
    }

    public static void e(String tag, Object content) {
        if (enable) {
            Log.e(tag, content.toString());
            System.out.println(content.toString());
        }
    }

    public static void print(Object content) {
        e("RIPPLE", content.toString());
    }

    public static void printForce(Object content) {
        System.out.println(content.toString());
    }

    public static void error(String tag, Exception e) {
        if (enable && e != null) {
            Log.e(tag, e.getMessage());
            System.out.println(e.getMessage());
        }
    }

}

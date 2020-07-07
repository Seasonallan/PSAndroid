package com.season.plugin.compat;


import com.season.lib.support.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 *  ActivityManager hook点 兼容API22以下
 */
public class ActivityManagerNativeCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.app.ActivityManagerNative");
        }
        return sClass;
    }

    public static Object getDefault() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeStaticMethod(Class(), "getDefault");
    }
}

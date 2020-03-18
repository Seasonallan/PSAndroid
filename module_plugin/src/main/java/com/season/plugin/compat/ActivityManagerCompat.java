package com.season.plugin.compat;

import com.season.lib.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * ActivityManager hook点 兼容API23以上
 */
public class ActivityManagerCompat {
    public static final int INTENT_SENDER_SERVICE = 4;

    public static final int INTENT_SENDER_ACTIVITY = 2;


    private static Class sClass;

    public static Class Class() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.app.ActivityManager");
        }
        return sClass;
    }

    public static Object getDefault() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeStaticMethod(Class(), "IActivityManagerSingleton");
    }

}

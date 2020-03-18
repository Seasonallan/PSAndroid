package com.season.plugin.compat;

import com.season.lib.reflect.FieldUtils;

/**
 * CompatibilityInfo 反射点
 */
public class CompatibilityInfoCompat {

    private static Class sClass;

    private static Class getMyClass() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.content.res.CompatibilityInfo");
        }
        return sClass;
    }

    private static Object sDefaultCompatibilityInfo;

    public static Object DEFAULT_COMPATIBILITY_INFO() throws IllegalAccessException, ClassNotFoundException {
        if (sDefaultCompatibilityInfo==null) {
            sDefaultCompatibilityInfo = FieldUtils.readStaticField(getMyClass(), "DEFAULT_COMPATIBILITY_INFO");
        }
        return sDefaultCompatibilityInfo;
    }
}

package com.season.plugin.parser;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;


import com.season.lib.util.LogUtil;
import com.season.plugin.compat.SystemPropertiesCompat;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * Disc: 包解析基类，适配SDK不同版本，构建generatePackageInfo
 * 用于创建loadApk：ActivityThread的方法getPackageInfoNoCheck的参数
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-05-22 13:34
 */
abstract class PackageParser {
    protected Context mContext;

    protected Object mPackageParser;

    PackageParser(Context context) {
        mContext = context;
    }


    public static PackageParser newPluginParser(Context context) throws Exception {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
            if ("1".equals(SystemPropertiesCompat.get("ro.build.version.preview_sdk", ""))) {
                return new PackageParserApi22Preview1(context);
            } else {
                return new PackageParserApi22(context);//API 20
            }
        } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return new PackageParserApi21(context);//API 21
        } else if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1 && VERSION.SDK_INT <= VERSION_CODES.KITKAT_WATCH) {
            return new PackageParserApi20(context);//API 17,18,19,20
        } else if (VERSION.SDK_INT == VERSION_CODES.JELLY_BEAN) {
            return new PackageParserApi16(context); //API 16
        } else if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH && VERSION.SDK_INT <= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return new PackageParserApi15(context); //API 14,15
        } else if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && VERSION.SDK_INT <= VERSION_CODES.HONEYCOMB_MR2) {
            return new PackageParserApi15(context); //API 11,12,13
        } else if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD && VERSION.SDK_INT <= VERSION_CODES.GINGERBREAD_MR1) {
            return new PackageParserApi15(context); //API 9，10
        } else {
            return new PackageParserApi15(context); //API 9，10
        }
    }

    public abstract void parsePackage(File file, int flags) throws Exception;

    public abstract void collectCertificates(int flags) throws Exception;

    public abstract ActivityInfo generateActivityInfo(Object activity, int flags) throws Exception;

    public abstract ServiceInfo generateServiceInfo(Object service, int flags) throws Exception;

    public abstract ProviderInfo generateProviderInfo(Object provider, int flags) throws Exception;

    public ActivityInfo generateReceiverInfo(Object receiver, int flags) throws Exception {
        return generateActivityInfo(receiver, flags);
    }

    public abstract InstrumentationInfo generateInstrumentationInfo(Object instrumentation, int flags) throws Exception;

    public abstract ApplicationInfo generateApplicationInfo(int flags) throws Exception;

    public abstract PermissionGroupInfo generatePermissionGroupInfo(Object permissionGroup, int flags) throws Exception;

    public abstract PermissionInfo generatePermissionInfo(Object permission, int flags) throws Exception;

    public abstract PackageInfo generatePackageInfo(int gids[], int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermissions) throws Exception;

    public abstract List getActivities() throws Exception;

    public abstract List getServices() throws Exception;

    public abstract List getProviders() throws Exception;

    public abstract List getPermissions() throws Exception;

    public abstract List getPermissionGroups() throws Exception;

    public abstract List getRequestedPermissions() throws Exception;

    public abstract List getReceivers() throws Exception;

    public abstract List getInstrumentations() throws Exception;

    public abstract String getPackageName() throws Exception;

    public abstract String readNameFromComponent(Object data) throws Exception;

    public abstract List<IntentFilter> readIntentFilterFromComponent(Object data) throws Exception;

    public abstract void writeSignature(Signature[] signatures) throws Exception;
}

package com.season.plugin.parser;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.season.lib.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Api22Preview1
 */
//for Android M
class PackageParserApi22Preview1 extends PackageParserApi21 {


    private static final String TAG = PackageParserApi22Preview1.class.getSimpleName();

    PackageParserApi22Preview1(Context context) throws Exception {
        super(context);
    }

    @Override
    public PackageInfo generatePackageInfo(
            int gids[], int flags, long firstInstallTime, long lastUpdateTime,
            HashSet<String> grantedPermissions) throws Exception {
        /*public static PackageInfo generatePackageInfo(PackageParser.Package p,
            int gids[], int flags, long firstInstallTime, long lastUpdateTime,
            HashSet<String> grantedPermissions, PackageUserState state, int userId) */
        try {
            return super.generatePackageInfo(gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions);
        } catch (Exception e) {
            Log.i(TAG, "generatePackageInfo fail", e);
        }
        Method method = MethodUtils.getAccessibleMethod(sPackageParserClass, "generatePackageInfo",
                mPackage.getClass(),
                int[].class, int.class, long.class, long.class, Set.class, sPackageUserStateClass, int.class);
        return (PackageInfo) method.invoke(null, mPackage, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions, mDefaultPackageUserState, mUserId);
    }
}

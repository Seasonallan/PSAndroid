package com.season.plugin.parser;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;


import com.season.lib.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

/**
 * Api22
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
class PackageParserApi22 extends PackageParserApi21 {
    private static final String TAG = PackageParserApi22Preview1.class.getSimpleName();

    PackageParserApi22(Context context) throws Exception {
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
            e.printStackTrace();
        }

        Method method = MethodUtils.getAccessibleMethod(sPackageParserClass, "generatePackageInfo",
                mPackage.getClass(),
                int[].class, int.class, long.class, long.class, sArraySetClass, sPackageUserStateClass, int.class);
        Object grantedPermissionsArray = null;
        try {
            Constructor constructor = sArraySetClass.getConstructor(Collection.class);
            grantedPermissionsArray = constructor.newInstance(constructor, grantedPermissions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (grantedPermissionsArray == null) {
            grantedPermissionsArray = grantedPermissions;
        }
        return (PackageInfo) method.invoke(null, mPackage, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissionsArray, mDefaultPackageUserState, mUserId);
    }
}

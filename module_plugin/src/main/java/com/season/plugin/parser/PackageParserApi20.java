package com.season.plugin.parser;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

import com.season.lib.support.reflect.MethodUtils;

import java.io.File;

/**
 * Api20
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class PackageParserApi20 extends PackageParserApi21 {


    public PackageParserApi20(Context context) throws Exception {
        super(context);
    }

    @Override
    public void parsePackage(File sourceFile, int flags) throws Exception {
        /* public Package parsePackage(File sourceFile, String destCodePath,
            DisplayMetrics metrics, int flags)*/
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        String destCodePath = sourceFile.getPath();
        mPackageParser = MethodUtils.invokeConstructor(sPackageParserClass, destCodePath);
        mPackage = MethodUtils.invokeMethod(mPackageParser, "parsePackage", sourceFile, destCodePath, metrics, flags);
    }
}

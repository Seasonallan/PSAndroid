package com.season.lib.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2018-09-12 18:11
 */
public class DimensionsUtil {
    public DimensionsUtil() {
    }

    public static int dip2px(float dipValue, Context context) {
        float context1 = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * context1 + 0.5F);
    }

    public static int px2dip(float pxValue, Context context) {
        float context1 = context.getResources().getDisplayMetrics().density;
        return (int)((pxValue - 0.5F) / context1);
    }

    public static void measureView(View view) {
        int var1 = View.MeasureSpec.makeMeasureSpec(0, 0);
        int var2 = View.MeasureSpec.makeMeasureSpec(0, 0);
        view.measure(var1, var2);
    }

    public static String getDeviceResolution(Context context) {
        DisplayMetrics context1 = context.getResources().getDisplayMetrics();
        return context1.widthPixels + "_" + context1.heightPixels;
    }

    public static DisplayMetrics getRealDisplayMetrics(Activity activity) {
        Display var1 = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics var2 = new DisplayMetrics();

        try {
            Class.forName("android.view.Display").getMethod("getRealMetrics", new Class[]{DisplayMetrics.class}).invoke(var1, new Object[]{var2});
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        if(var2.widthPixels <= 0 || var2.heightPixels <= 0) {
            var2.widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
            var2.heightPixels = activity.getResources().getDisplayMetrics().heightPixels;
        }

        return var2;
    }
}

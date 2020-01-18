package com.season.lib.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


import java.lang.reflect.Method;

/**
 * pd、px、sp互转
 */
public class DimenUtil {

    private static DisplayMetrics mDisplayMetrics;

    public static int getVirtualBarHeight(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked") Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - display.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return vh;
    }

    /**
     * 获取屏幕的宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        if (mDisplayMetrics == null) {
            mDisplayMetrics = context.getResources().getDisplayMetrics();
        }
        return mDisplayMetrics.widthPixels;
    }

    /**
     * 获取屏幕的高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        if (mDisplayMetrics == null) {
            mDisplayMetrics = context.getResources().getDisplayMetrics();
        }
        return mDisplayMetrics.heightPixels;
    }

    public static int getScreenRealHeight(Activity activity) {
        if (Build.VERSION.SDK_INT >= 17) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            Point size = new Point();
            display.getRealSize(size);
            return size.y;
        }
        return getScreenHeight(activity);
    }

    public static float getScreenScale(Activity activity) {
        return getScreenRealHeight(activity) * 1.0f / getScreenWidth(activity);
    }

//    public static boolean getIs18_9() {
//        //???已经扣掉虚拟按键高度
//        int screenHeight1 = com.blankj.utilcode.utils.ScreenUtils.getScreenHeight();
//        int screenWidth1 = com.blankj.utilcode.utils.ScreenUtils.getScreenWidth();
//        float screenHeight = getScreenHeight();
//        float screenWidth = getScreenWidth();
//        return   screenHeight/ screenWidth == 18f / 9f;//18:9
//    }


    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据图片原始宽度和调整后的宽度得到调整后的高度
     *
     * @param originalWidth  原始宽度
     * @param originalHeight 原始高度
     * @param adjustWidth    调整后的宽度
     * @return 调整后的高度
     */
    public static int getAdjustHeight(int originalWidth, int originalHeight,
                                      int adjustWidth) {
        float temp = (float) originalWidth / (float) adjustWidth;
        int adjustHeight = (int) ((float) originalHeight / temp);
        return adjustHeight;
    }

    /**
     * 按比例获取长度
     * <p/>
     * refSize /result == refRatio/resultRadio
     *
     * @param refSize     参考长度
     * @param refRatio    参考比例
     * @param resultRadio 结果值比例
     * @return
     */
    public static int getSizeByScale(int refSize, int refRatio, int resultRadio) {
        return (int) ((float) refSize * resultRadio / refRatio);
    }

    public static float getDimension(Context context, int resId) {
        return context.getResources().getDimension(resId);
    }
}

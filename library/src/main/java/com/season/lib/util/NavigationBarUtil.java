package com.season.lib.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.WindowManager;

/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2018-09-13 20:14
 */
public class NavigationBarUtil {



    /**
     * 获取虚拟按键的高度
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void hideNavigationBar(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);

        View decorView = activity.getWindow().getDecorView();
        int uiOptions =  View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static void showNavigationBar(Activity activity) {

        View decorView = activity.getWindow().getDecorView();
        int uiOptions =  View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

        decorView.setSystemUiVisibility(uiOptions);
    }

}

package com.season.lib.util;

import android.content.Context;
import android.content.res.Configuration;


/**
 * Created by Administrator on 2017/11/22.
 */

public class ToolPaint {

    private static ToolPaint defaultInstance;
    public static ToolPaint getDefault() {
        if (defaultInstance == null) {
            synchronized (ToolPaint.class) {
                if (defaultInstance == null) {
                    defaultInstance = new ToolPaint();
                }
            }
        }
        return defaultInstance;
    }

    ToolPaint(){

    }



    public int paintWidth = -1;
    public int getPaintWidth(Context context){
        if (paintWidth <= 0){
            paintWidth = AutoUtils.getPercentWidthSize(32);
            if (isPad(context)){
                paintWidth = paintWidth/2;
            }
        }
        return paintWidth;
    }

    public int strokeWidth = -1;
    public int getStrokeWidth(){
        if (strokeWidth <= 0){
            strokeWidth = AutoUtils.getPercentWidthSize(18);
        }
        return strokeWidth;
    }


    private float maxTextLength = -1;
    public float getMaxTextLength(Context context){
        if (maxTextLength <= 0){
            maxTextLength = ScreenUtils.getScreenWidth(context) * 2.75f;
        }
        return maxTextLength;
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public float getScale(Context context, int singleLinemaxCount){
        int paintSize = getPaintSize(context);
        int showWidth = paintSize * singleLinemaxCount;
        int screenWidth = ScreenUtils.getScreenWidth(context);

        if (singleLinemaxCount<=9){
//        if (singleLinemaxCount<=10||showWidth < screenWidth){
            return 0.5f;
        }else{
            return (screenWidth*10/11) * 1.0f/showWidth;
//            return (screenWidth-paintSize) * 1.0f/showWidth;
        }
    }
    int paintSize = -1;
    String tag=ToolPaint.this.getClass().getName();
    public int getPaintSize(Context context){
        if (paintSize <= 0){
            //参照line，这个数值一行最大显示10个字,还有留下间距
            /**
             * 默认多放大一倍，再缩小下来
             */
            paintSize = ScreenUtils.getScreenWidth(context)*2/12;
//            paintSize = ScreenUtils.getScreenWidth()*225/1000;
            if (isPad(context)){
                //PAD字体太大会出现描边错位的问题
                paintSize = paintSize/2;
            }
        }
        Logger.d(tag+":getPaintSize==>"+paintSize);
        return paintSize;
    }
}

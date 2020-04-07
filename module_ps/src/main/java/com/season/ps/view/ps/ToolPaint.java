package com.season.ps.view.ps;


import com.season.lib.dimen.ScreenUtils;

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
    public int getPaintWidth(){
        if (paintWidth <= 0){
            paintWidth = 32;
            if (ScreenUtils.isPad()){
                paintWidth = paintWidth/2;
            }
        }
        return paintWidth;
    }

    public int strokeWidth = -1;
    public int getStrokeWidth(){
        if (strokeWidth <= 0){
            strokeWidth = 18;
        }
        return strokeWidth;
    }


    public float getScale(int singleLinemaxCount){
        int paintSize = getPaintSize();
        int showWidth = paintSize * singleLinemaxCount;
        int screenWidth = ScreenUtils.getScreenWidth();

        if (singleLinemaxCount<=9){
            return 0.5f;
        }else{
            return (screenWidth*10/11) * 1.0f/showWidth;
        }
    }

    int paintSize = -1;
    public int getPaintSize(){
        if (paintSize <= 0){
            //参照line，这个数值一行最大显示10个字,还有留下间距
            /**
             * 默认多放大一倍，再缩小下来
             */
            paintSize = ScreenUtils.getScreenWidth()*2/12;
//            paintSize = ScreenUtils.getScreenWidth()*225/1000;
            if (ScreenUtils.isPad()){
                //PAD字体太大会出现描边错位的问题
                paintSize = paintSize/2;
            }
        }
        return paintSize;
    }
}

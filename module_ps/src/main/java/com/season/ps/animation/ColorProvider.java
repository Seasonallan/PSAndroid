package com.season.ps.animation;


import android.graphics.Canvas;

/**
 * Disc: 动效：颜色变化 暂时弃用
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class ColorProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "ColorProvider";
    }

    @Override
    public int getDuration() {
        return getDelay() * 2;
    }

    int position = 0;
    int color1 = 0xFFff8f34;
    int color2 = 0xffffb80f;
    @Override
    public int getColor() {
        if (position == 0){
            position = 1;
            return color1;
        }else{
            position = 0;
            return color2;
        }
    }

    @Override
    public void proCanvas(Canvas canvas) {

    }
}

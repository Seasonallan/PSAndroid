package com.season.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：行动画，如果需要对每行做动画，取余行动画的perRowTime 别名：
 * User: lizhongxin
 * Time: 2017-12-26
 */
public class SingleLineProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "SingleLineProvider";
    }


    @Override
    public boolean isRowSplited(){
        return true;
    }


    @Override
    public boolean isRepeat() {
        return true;
    }
    int wordDelay = 50;

    @Override
    public int getDuration() {
        return rowCount * (950 + wordDelay);
    }


    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
    }

    @Override
    public void proCanvas(Canvas canvas) {
    }

    @Override
    public int setTime(int time, boolean record) {
        return super.setTime(time, record);
    }

}

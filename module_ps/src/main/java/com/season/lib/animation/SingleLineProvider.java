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
    /**
     * 每个字有不同的动画
     * @return
     */
    @Override
    public boolean isWordSplit(){
        return false;
    }
    @Override
    public boolean isRowSplited(){
        return true;
    }
    @Override
    public boolean isSingleLine(){
        return true;
    }

    int wordDelay = 50;
    int count = 4;
    int alpha = 255;
    @Override
    public int getDuration() {
        return getPerTime() * (totalSize + 1)+ stayTime;
    }

    private int getPerTime(){
        return getDelay() * count + wordDelay;
    }

    @Override
    public void init() {
        wordDelay = (totalTime - stayTime)/ (totalSize + 1) - getDelay() * count;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
    }

    @Override
    public void proCanvas(Canvas canvas) {
        canvas.restore();
    }

    @Override
    public int setTime(int time, boolean record) {
        return super.setTime(time, record);
    }

}

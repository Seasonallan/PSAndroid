package com.seaon.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：跑马灯 暂时弃用
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class HorseLampProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "HorseLampProvider";
    }
    float dx = 0;
    @Override
    public int getDuration() {
        return totalTime;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.translate(dx, 0);
    }

    @Override
    public void proCanvas(Canvas canvas) {
        canvas.restore();
    }


    @Override
    public int setTime(int time, boolean record) {
        if (record){
            time = time % getDuration();
        }
        float perX = (textWidth * 1.2f) * 2.0f/getDuration();
        dx = (textWidth * 1.2f) - perX * time;
        return super.setTime(time, record);
    }

    @Override
    public int getDelay() {
        return delayDefault;
    }

}

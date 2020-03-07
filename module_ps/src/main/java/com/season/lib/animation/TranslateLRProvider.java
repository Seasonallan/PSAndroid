package com.season.lib.animation;

import android.graphics.Canvas;

/**
 * Disc: 动效：左右晃动
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class TranslateLRProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "TranslateLRProvider";
    }

    float dx = 0;

    @Override
    public int getDuration() {
        return 500;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.translate(dx, 0);
    }


    @Override
    public int setTime(int time, boolean record) {
        int duration = getDuration();
        if (time < duration/2){
            float percent = time * 1.0f/ (duration/2);
            dx = transMax - percent * transMax * 2;
        }else{
            float percent = (time - duration/2) * 1.0f/ (duration/2);
            dx = - transMax + percent * transMax * 2;
        }
        return super.setTime(time, record);
    }

}

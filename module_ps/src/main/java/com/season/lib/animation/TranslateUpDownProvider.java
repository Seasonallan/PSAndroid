package com.season.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：上下颠簸
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class TranslateUpDownProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "TranslateUpDownProvider";
    }
    @Override
    public boolean isRepeat() {
        return true;
    }

    @Override
    public int getDuration() {
        return 500;
    }

    float dy = 0;

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.translate(0, dy);
    }

    @Override
    public int setTime(int time, boolean record) {
        int duration = getDuration();
        if (time < duration/2){
            float percent = time * 1.0f/ (duration/2);
            dy = transMax - percent * transMax * 2;
        }else{
            float percent = (time - duration/2) * 1.0f/ (duration/2);
            dy = - transMax + percent * transMax * 2;
        }
        return super.setTime(time, record);
    }

}

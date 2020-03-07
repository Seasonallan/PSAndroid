package com.season.lib.animation;

import android.graphics.Canvas;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

/**
 * Disc: 动效：底部升起
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class TranslateShowUpProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "TranslateShowUpProvider";
    }

    @Override
    public boolean isRepeat() {
        return false;
    }
    float upTime;
    Interpolator decelerateInterpolator;
    @Override
    public void init() {
        decelerateInterpolator = new BounceInterpolator();
        upTime = (getDuration() - stayTime);
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.translate(0, dy);
    }

    float dy = 0;
    @Override
    public int setTime(int time, boolean record) {
        if (time >= upTime){
            dy = 0;
        }else{
            dy = textHeight - textHeight * decelerateInterpolator.getInterpolation(time * 1.0f/upTime);
        }
        return super.setTime(time, record);
    }

}

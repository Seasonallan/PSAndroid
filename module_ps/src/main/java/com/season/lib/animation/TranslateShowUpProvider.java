package com.season.lib.animation;

import android.graphics.Canvas;
import android.view.animation.DecelerateInterpolator;

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

    float dy = 0;
    @Override
    public int getDuration() {
        return totalTime * 3 / 5;
    }
    @Override
    public int getDelay() {
        if (delayDefault<=60){
            return 85;
        }
        return delayDefault;
    }
    @Override
    public boolean clipPath() {
        return true;
    }

    float speed = 5;

    float upTime = 800;
    float upTimeCount = 4;
    float downTimeCount = 3;
    @Override
    public void init() {
        upTime = 1500 - stayTime - getDelay() * downTimeCount;
        upTimeCount = speed * 1500 * 1.0f/1500;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.translate(0, dy);
    }

    @Override
    public void proCanvas(Canvas canvas) {
        canvas.restore();
    }

    @Override
    public int setTime(int time, boolean record) {
        if (time > getDuration()){
            dy = 0;
            return super.setTime(time, record);
        }
        if (time < getDelay()){
            dy = 0;
        }else{
            if (time < upTime){
                float percentTime = new DecelerateInterpolator().getInterpolation(time * 1.0f/upTime);
                dy = textHeight* upTimeCount * (1 - percentTime) - transMax;
            }else if (time <= upTime + getDelay() *  downTimeCount){
                dy = - transMax + transMax * (time - upTime)/(getDelay() * downTimeCount);
            }else{
                dy = 0;
            }
        }
        return super.setTime(time, record);
    }

}

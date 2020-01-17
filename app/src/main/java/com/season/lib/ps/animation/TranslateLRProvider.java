package com.season.lib.ps.animation;

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

    @Override
    public boolean isRepeat() {
        return true;
    }

    float dx = 0;
    float count = 5f;
    @Override
    public int getDuration() {
        return (int) (getDelay() * count * 2);
    }

    @Override
    public void init(){
        count = 5f;
        int perSize = totalTime/getDuration();
        if (totalTime % getDuration() != 0)
            perSize ++;
        float realDuration = totalTime * 1.0f/perSize;
        count = realDuration/(getDelay() * 2);
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
        int duration = getDuration();
        if (record){
            time = time % duration;
        }
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

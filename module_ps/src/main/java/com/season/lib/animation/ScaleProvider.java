package com.season.lib.animation;

import android.graphics.Canvas;

/**
 * Disc: 动效：放大缩小
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class ScaleProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "ScaleProvider";
    }

    @Override
    public boolean isRepeat() {
        return true;
    }

    final float scaleMax = 0.2f;
    float scale = 1;
    float count = 5;
    @Override
    public int getDuration() {
        return (int) (getDelay() * count * 2);
    }

    @Override
    public void init(){
        count = 5;
        int perSize = totalTime/getDuration();
        if (totalTime % getDuration() != 0)
            perSize ++;
        float realDuration = totalTime * 1.0f/perSize;
        count = realDuration/(getDelay() * 2);
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.scale(scale, scale, centerX, centerY);
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
        if (time >= getDuration()/2){//缩小
            float percent = (time - getDuration()/2) * 1.0f/ (getDuration()/2);
            scale = 0.82f + scaleMax - percent * scaleMax;
        }else{
            float percent = time * 1.0f/ (getDuration()/2);
            scale = 0.82f + percent * scaleMax;
        }
        return super.setTime(time, record);
    }

}

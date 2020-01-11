package com.season.lib.animation;

import android.graphics.Canvas;

import com.season.lib.util.Logger;


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

    float dy = 0;
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
        canvas.translate(0, dy);
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
            Logger.d("record>> time="+ time);
        }
        if (time < duration/2){
            float percent = time * 1.0f/ (duration/2);
            dy = transMax - percent * transMax * 2;
        }else{
            float percent = (time - duration/2) * 1.0f/ (duration/2);
            dy = - transMax + percent * transMax * 2;
        }
        if (record){
            Logger.d("record>> dx="+ dy);
        }
        return super.setTime(time, record);
    }

}

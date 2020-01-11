package com.season.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：波浪跳动
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class WaveProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "WaveProvider";
    }

    @Override
    public boolean isRepeat() {
        return true;
    }

    float dy = 0;
    float count = 2f;

    /**
     * 每个字有不同的动画
     * @return
     */
    @Override
    public boolean isWordSplited(){
        return true;
    }

    @Override
    public void init() {
        count = 2f;
        int perSize = totalTime/getDuration();
        float realDuration = totalTime * 1.0f/perSize;
        count = realDuration/(getDelay() * 4);
    }

    @Override
    public int getDuration() {
        return (int) (getDelay() * count * 4);
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
        if (record){
            time = time % getDuration();
        }
        int perTime = getDuration()/totalSize;
        time += perTime * position;
        time = time % getDuration();
        if (time < getDuration()/4){//缩小
            float percent = time * 1.0f/ (getDuration()/4);
            dy = - percent * transMax;
        }else if (time < getDuration()/2){
            float percent = (time - getDuration()/4) * 1.0f/ (getDuration()/4);
            dy = -transMax + percent * transMax;
        }else if (time < getDuration() * 3/4){
            float percent = (time - getDuration()* 2/4) * 1.0f/ (getDuration()/4);
            dy = percent * transMax;
        }else{
            float percent = (time - getDuration()* 3/4) * 1.0f/ (getDuration()/4);
            dy = transMax - percent * transMax;
        }
        return super.setTime(time, record);
    }

}

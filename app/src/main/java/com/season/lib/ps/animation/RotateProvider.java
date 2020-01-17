package com.season.lib.ps.animation;

import android.graphics.Canvas;

/**
 * Disc: 动效：上下摇晃 暂时弃用
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class RotateProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "RotateProvider";
    }
    @Override
    public boolean isRepeat() {
        return true;
    }

    float rotateDegree = 8f;
    float degree = 0;
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
        rotateDegree = 8  - totalSize * 0.5f;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.rotate(degree, centerX, centerY);
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
            degree =  - rotateDegree + percent * rotateDegree * 2;
        }else{
            float percent = time * 1.0f/ (getDuration()/2);
            degree = rotateDegree - percent * rotateDegree * 2;
        }
        return super.setTime(time, record);
    }

}

package com.season.ps.animation;

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

    float rotateDegree = 8f;
    float degree = 0;
    @Override
    public int getDuration() {
        return 500;
    }

    @Override
    public void init(){
        rotateDegree = 8  - totalSize * 0.5f;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.rotate(degree, centerX, centerY);
    }

    @Override
    public int setTime(int time, boolean record) {
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

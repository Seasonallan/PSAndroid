package com.example.lib.animation;

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

    final float scaleMax = 0.2f;
    float scale = 1;

    @Override
    public int getDuration() {
        return 500;
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

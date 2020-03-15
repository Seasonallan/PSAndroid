package com.season.ps.animation;

import android.graphics.Canvas;

/**
 * Disc: 动效：逐字放大
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class LineUpScaleProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "LineUpScaleProvider";
    }

    @Override
    public boolean isWordSplit() {
        return true;
    }

    @Override
    public boolean isRepeat() {
        return true;
    }
    int wordDelay = 50;//每个字之间的延迟， 不同的时长的视频，这个值会动态改变，以达到动画效果凑满视频时长
    final float scaleMax = 0.4f;
    float scale = 1;

    @Override
    public int getDuration() {
        return getPerTime() * totalSize + stayTime;
    }

    private int getPerTime() {
        return 150 + wordDelay;
    }


    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.scale(scale, scale, centerX, centerY);
    }


    @Override
    public int setTime(int time, boolean record) {
        int perTime = getPerTime();
        int display = time / perTime;
        if (display == position) {
            time = time % perTime;
            if (time > perTime - wordDelay) {
                scale = 1;
            } else {
                if (time < (perTime - wordDelay) / 2) {//缩小
                    float percent = time * 1.0f / ((perTime - wordDelay) / 2);
                    scale = 1 + percent * scaleMax;
                } else {
                    float percent = (time - (perTime - wordDelay) / 2) * 1.0f / ((perTime - wordDelay) / 2);
                    scale = 1 + scaleMax - percent * scaleMax;
                }
            }
        } else {
            scale = 1;
        }
        return super.setTime(time, record);
    }

}

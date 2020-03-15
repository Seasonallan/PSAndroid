package com.example.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：放大淡出显示 别名：排队登场
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class LineUpScaleAlphaProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "LineUpScaleAlphaProvider";
    }

    @Override
    public boolean isRepeat() {
        return true;
    }

    @Override
    public boolean isWordSplit(){
        return true;
    }

    int wordDelay = 50;//每个字之间的延迟， 不同的时长的视频，这个值会动态改变，以达到动画效果凑满视频时长
    final float scaleMax = 0.4f;
    float scale = 1;
    int alpha = 0;
    @Override
    public int getDuration() {
        return getPerTime() * (totalSize + 1)+ stayTime;
    }

    private int getPerTime(){
        return 150 + wordDelay;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public void preCanvas(Canvas canvas, int centerX, int centerY) {
        canvas.save();
        canvas.scale(scale, scale, centerX, centerY);
    }

    @Override
    public int setTime(int time, boolean record) {
        int perTime = getPerTime();
        int display = time/perTime;

        if (display == position){
            time = time % perTime;
            if (time > perTime - wordDelay){
                scale = 1;
                alpha = 255;
            }else{
                if (time < (perTime - wordDelay)/2){//缩小
                    float percent = time * 1.0f/ ((perTime - wordDelay)/2);
                    scale = 1 + percent * scaleMax;
                    alpha = (int) (percent * 130);
                }else{
                    float percent = (time - (perTime - wordDelay)/2) * 1.0f/ ((perTime - wordDelay)/2);
                    scale = 1 + scaleMax - percent * scaleMax;
                    alpha = 130 + (int) (percent * 125);
                }
            }
        }else{
            if (display > position){
                scale = 1;
                alpha = 255;
            }else{
                scale = 0;
                alpha = 0;
            }
        }
        return super.setTime(time, record);
    }

}

package com.season.ps.animation;

import android.graphics.Canvas;

/**
 * Disc: 动效：排队出现
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class LineUpShowProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "LineUpShowProvider";
    }

    @Override
    public boolean isWordSplit(){
        return true;
    }

    @Override
    public boolean isRepeat() {
        return true;
    }
    @Override
    public int getAlpha() {
        return alpha;
    }

    int wordDelay = 50;
    int alpha = 0;
    float scale = 1;
    @Override
    public int getDuration() {
        return getPerTime() * totalSize + stayTime;
    }

    private int getPerTime(){
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
        int display = time/perTime;

        if (display == position){
            scale = 1;
            time = time % perTime;
            if (time > perTime - wordDelay){
                alpha = 255;
            }else{
                if (time < (perTime - wordDelay)/2){//缩小    (122-59)/2
                    float percent = time * 1.0f/ ((perTime - wordDelay)/2);
                    alpha = (int) (percent * 130);
                }else{
                    float percent = (time - (perTime - wordDelay)/2) * 1.0f/ ((perTime - wordDelay)/2);
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

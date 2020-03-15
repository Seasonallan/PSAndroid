package com.example.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：逐字跳动
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class WaveOneProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "WaveOneProvider";
    }

    @Override
    public boolean isWordSplit(){
        return true;
    }

    @Override
    public boolean isRepeat() {
        return true;
    }
    int wordDelay = 50;
    float dy = 0;

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
        canvas.translate(0, dy);
    }

    @Override
    public int setTime(int time, boolean record) {
        int perTime = getPerTime();
        int display = time/perTime;
        if (display == position){
            time = time % perTime;
            if (time > perTime - wordDelay){
                dy = transMax;
            }else{
                if (time < (perTime - wordDelay)/2){
                    float percent = time * 1.0f/ ((perTime - wordDelay)/2);
                    dy = transMax - percent * transMax * 2;
                }else{
                    float percent = (time - (perTime - wordDelay)/2) * 1.0f/ ((perTime - wordDelay)/2);
                    dy = percent * transMax * 2 - transMax;
                }
            }
        }else{
            dy = transMax;
        }
        return super.setTime(time, record);
    }

}

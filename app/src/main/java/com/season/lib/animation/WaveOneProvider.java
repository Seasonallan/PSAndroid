package com.season.lib.animation;

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
    /**
     * 每个字有不同的动画
     * @return
     */
    @Override
    public boolean isWordSplited(){
        return true;
    }

    int wordDelay = 50;
    float dy = 0;
    int count = 4;

    @Override
    public void init() {
        wordDelay = (totalTime - stayTime)/totalSize - getDelay() * count;
        //wordDelay = Math.min(wordDelay, 100);
    }

    @Override
    public int getDuration() {
        return getPerTime() * totalSize + stayTime;
    }

    private int getPerTime(){
        return getDelay() * count + wordDelay;
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
        int perTime = getPerTime();
        int display = time/perTime;
        if (display == position){
            time = time % perTime;
            if (time > getDelay() * count){
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

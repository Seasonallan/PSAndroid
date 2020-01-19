package com.season.lib.animation;

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
    /**
     * 每个字有不同的动画
     * @return
     */
    @Override
    public boolean isWordSplited(){
        return true;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    int wordDelay = 50;
    int count = 4;
    int alpha = 0;
    float scale = 1;
    @Override
    public int getDuration() {
        return getPerTime() * (totalSize + 1)+ stayTime;
    }

    private int getPerTime(){
        return getDelay() * count + wordDelay;
    }

    @Override
    public void init() {
        wordDelay = (totalTime - stayTime)/ (totalSize + 1) - getDelay() * count;
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
        int perTime = getPerTime();//122
        int display = time/perTime;
//        Log.d("setTime1","perTime:"+perTime+",display:"+display+"，time："+time);
        if (display == 0){
            scale = 1;
            alpha = 255;
            return super.setTime(time, record);
        }
        if (display == 1){
            return 0;
        }
        display -= 2;
//        Log.d("setTime2","position:"+position+",display:"+display+"，time："+time);
        if (display == position){
            scale = 1;
            int time_copy=time;
            time = time % perTime;
//            Log.d("setTime3","time_copy:"+time_copy+"，time："+time);
            if (time > getDelay() * count){
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

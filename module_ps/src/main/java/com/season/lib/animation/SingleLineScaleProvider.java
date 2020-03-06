package com.season.lib.animation;

import android.graphics.Canvas;
import android.util.Log;


/**
 * Disc: 动效： 别名：
 */
public class SingleLineScaleProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "SingleLineScaleProvider";
    }
    /**
     * 每个字有不同的动画
     * @return
     */
    @Override
    public boolean isWordSplit(){
        return false;
    }
    @Override
    public boolean isRowSplited(){
        return true;
    }
    @Override
    public boolean isSingleLine(){
        return true;
    }
    @Override
    public int getAlpha() {
        return alpha;
    }

    int wordDelay = 50;
    int count = 4;
    int alpha = 255;
    @Override
    public int getDuration() {
        stayTime=500;
        return getPerTime() *2* (totalSize + 1)+ stayTime;
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
    }

    @Override
    public void proCanvas(Canvas canvas) {
        canvas.restore();
    }

    @Override
    public int setTime(int time, boolean record) {
        float percent = ((float) time % perRowTime)/perRowTime;
        Log.d("setTime","time:"+time+",percent:"+percent+",perRowTime:"+perRowTime);
        if(percent<0.2){
            alpha= (int) (255*(percent/0.2f));
        }else if(percent>=0.2&&percent<0.5)
        {
            alpha=255;
        }else
        {
            alpha= (int) ((1-(percent-0.5f)/0.5f)*255f);
        }
        return super.setTime(time, record);
    }

}

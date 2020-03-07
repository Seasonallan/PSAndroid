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


    @Override
    public boolean isRowSplited(){
        return true;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    int wordDelay = 50;
    int alpha = 255;

    @Override
    public int getDuration() {
        return rowCount * (950 + wordDelay);
    }

    @Override
    public void proCanvas(Canvas canvas) {
    }

    @Override
    public int setTime(int time, boolean record) {
        float percent = ((float) time % getPerRowTime())/getPerRowTime();
        if(percent<0.2){
            alpha = (int) (255*(percent/0.2f));
        }else if(percent>=0.2&&percent<0.5)
        {
            alpha =255;
        }else
        {
            alpha= (int) ((1-(percent-0.5f)/0.5f)*255f);
        }
        return super.setTime(time, record);
    }

}

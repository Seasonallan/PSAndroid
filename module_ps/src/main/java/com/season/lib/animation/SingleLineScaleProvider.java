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
    public boolean isWordSplited(){
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
//        int rowPertime=getVideoDuration()/allrownum;
//        //第一行 0～rowPertime
//        //第二行 1～2rowPertime
//        //第i行 i-1~irowPertime
//        int perTime = getPerTime();
//        int display = time/perTime;//第几个字
//        if (display == 0){
//            alpha = 255;
//            return super.setTime(time, record);
//        }
//        if (display == 1){
//            return 0;
//        }
//        display -= 2;
//        if (display == position){
//            time = time % perTime;
//            if (time > getDelay() * count){
//                alpha = 255;
//            }else{
//                if (time < (perTime - wordDelay)/2){//缩小
//                    float percent = time * 1.0f/ ((perTime - wordDelay)/2);
////                    alpha = (int) (percent * 130);
//                }else{
//                    float percent = (time - (perTime - wordDelay)/2) * 1.0f/ ((perTime - wordDelay)/2);
////                    alpha = 130 + (int) (percent * 125);
//                }
//            }
//        }else{
//            if (display > position){
//                alpha = 255;
//            }else{
//                alpha = 0;
//            }
//        }
        return super.setTime(time, record);
    }

}

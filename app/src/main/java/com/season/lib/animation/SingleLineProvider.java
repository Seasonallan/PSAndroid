package com.season.lib.animation;

import android.graphics.Canvas;


/**
 * Disc: 动效：行动画，如果需要对每行做动画，取余行动画的perRowTime 别名：
 * User: lizhongxin
 * Time: 2017-12-26
 */
public class SingleLineProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "SingleLineProvider";
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
//    @Override
//    public int getAlpha() {
//        return alpha;
//    }

    int wordDelay = 50;
    int count = 4;
    int alpha = 255;
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
    }

    @Override
    public void proCanvas(Canvas canvas) {
        canvas.restore();
    }

    @Override
    public int setTime(int time, boolean record) {
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

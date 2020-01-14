package com.season.playball.sin.interpolator;

import java.util.Random;

/**
 * Disc: 速度控制工厂
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 15:48
 */
public class BallInterpolatorFactory {
    public final static String LINEAR = "LinearInterpolator";
    public final static String KEEP = "Keep";
    public final static String ACCELERATE = "Accelerate";

    public static String randomFlag(){
        String flag = LINEAR;
        switch (new Random().nextInt(3)){
            case 0:
                flag = LINEAR;
                break;
            case 1:
                flag = KEEP;
                break;
            case 2:
                flag = ACCELERATE;
                break;
        }
        return flag;
    }

    /**
     * 获取指定加速器
     * @param flag
     * @return
     */
    public static IInterpolator getInterpolator(String flag){
        IInterpolator interpolator = new LinearInterpolator();
        switch (flag){
            case LINEAR:
                interpolator =  new LinearInterpolator();
                break;
            case KEEP:
                interpolator =  new KeepInterpolator();
                break;
            case ACCELERATE:
                interpolator =  new AccelerateInterpolator();
                break;
        }
        return interpolator;
    }
}

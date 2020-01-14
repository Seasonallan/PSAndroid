package com.season.playball.sin.interpolator;

/**
 * Disc: 加速
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 14:19
 */
public class AccelerateInterpolator extends BaseInterpolator {

    @Override
    public String getDescription() {
        return "加速";
    }

    @Override
    public void speedCost() {
        if (speed > 0){
            speed += 0.1;
        }
    }

}

package com.season.playball.sin.interpolator;

/**
 * Disc: 减速
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 14:19
 */
public class LinearInterpolator extends BaseInterpolator {

    @Override
    public String getDescription() {
        return "减速";
    }

    @Override
    public void speedCost() {
        if (speed > 0){
            speed -= 0.01;
        }
        speed = Math.max(0, speed);
    }

}

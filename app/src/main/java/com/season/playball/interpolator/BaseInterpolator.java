package com.season.playball.interpolator;

import java.util.Random;

/**
 * Disc: 速度控制基类
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 15:58
 */
public abstract class BaseInterpolator implements IInterpolator {
    float speed = 4;

    @Override
    public void randomSet(){
        speed = new Random().nextInt(8) + 8;
    }

    @Override
    public float getSpeed(){
        return speed;
    }

    @Override
    public void resetSpeed(float speed){
        this.speed = speed;
    }

    @Override
    public void speedChange(int speedCost, IInterpolator ballInterpolator) {
        if (true){
            speed = (speed + ballInterpolator.getSpeed())/2;
            if (speed <= 0){
                speed = 10;//填满屏幕的时候出现叠加，一直找位置
            }
           // LogConsole.log("speedChange current="+speed+"  other="+ballInterpolator.getSpeed());
            return;
        }
        if (speedCost< 180){//same area, speed up
            speed += ballInterpolator.getSpeed() * speedCost/360;
        }else{//speed down
            speed -= speed * speedCost/360;
        }

    }

}

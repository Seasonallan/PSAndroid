package com.season.lib.animation;

/**
 * Disc: 动效：突然闪现
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class FlushProvider extends AnimationProvider {
    @Override
    public String getClassName() {
        return "FlushProvider";
    }

    @Override
    public int getDuration() {
        return 500;
    }

    @Override
    public int setTime(int time, boolean record) {
        time = time % getDuration();
        if (time < getDuration()/2){ //250毫秒之前显示， 250-500毫秒隐藏
            return super.setTime(time, record);
        }
        return 0;
    }
}

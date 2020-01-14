package com.season.playball.mul;

import java.util.List;

/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 12:17
 */
public interface IBallView {

    int getId();

    int getScreenWidth();

    int getScreenHeight();

    void invalidate();

    List<BallView> getRunningBalls();
}

package com.season.playball.mul;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 11:59
 */
public class BallView extends View implements IBallView {

    int screenWidth, screenHeight;
    int id;

    BallController ballController;
    public BallView(Context context, int id, View parentView) {
        super(context);
        this.id = id;
        screenHeight = parentView.getHeight();
        screenWidth = parentView.getWidth();

        ballController = new BallController(this);
    }

    public int getId(){
        return id;
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    public void stop() {
        ballController.stop();
    }

    public void start() {
        ballController.start();
    }

    @Override
    public List<BallView> getRunningBalls() {
        return new ArrayList<>();
    };


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ballController.onDraw(canvas);
    }

}

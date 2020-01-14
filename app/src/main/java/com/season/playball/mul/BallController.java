package com.season.playball.mul;

import android.graphics.Canvas;
import android.os.Handler;

/**
 * Disc:
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 12:11
 */
public class BallController {

    IBallView ballView;
    BallModel ballModel;

    public BallController(BallView ballView) {
        this.ballView = ballView;
        this.ballModel = new BallModel(ballView.getScreenWidth(), ballView.getScreenHeight());
        this.ballModel.randomSetUp();
    }

    private boolean running = true;
    int time = 10;
    public void stop() {
        running = false;
        handler.removeMessages(1);
    }

    public void start() {
        running = true;
        this.ballModel.move();
        crashCheck();
        this.ballView.invalidate();
        handler.sendEmptyMessageDelayed(1, time);
    }

    void crashCheck() {
        for (BallView itemView : ballView.getRunningBalls()) {
            if (itemView.id != ballView.getId()) {
                BallModel itemModel = itemView.ballController.ballModel;
                if (ballModel.isCrash(itemModel)) {
                    ballModel.crashChanged(itemModel);
                }
            }
        }
    }


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (running) {
                start();
            }
        };
    };

    protected void onDraw(Canvas canvas) {
        ballModel.draw(canvas);
    }


}

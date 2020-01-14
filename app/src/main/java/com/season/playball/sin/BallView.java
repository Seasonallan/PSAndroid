package com.season.playball.sin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.season.playball.LogConsole;
import com.season.playball.sin.interpolator.BallInterpolatorFactory;
import com.season.playball.sin.interpolator.IInterpolator;

import java.util.ArrayList;
import java.util.List;


/**
 * Disc: 单个View多个球体
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 11:59
 */
public class BallView extends View {

    Paint paint;
    List<Ball> ballList;


    private boolean running = true;
    int time = 10;


    public BallView(Context context) {
        super(context);
        init();
    }
    public BallView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        ballList = new ArrayList<>();
        paint = new Paint();
        paint.setStrokeWidth(10);
    }

    public void stop() {
        running = false;
        handler.removeMessages(1);
    }

    public void start() {
        running = false;
        if (touchBall != null && touchBall.isTouched) {
            running = true;
        }
        for (Ball ballModel : ballList) {
            if (ballModel.hasSpeed()){
                ballModel.move();
                running = true;
            }
        }
        crashCheck();
        invalidate();
        handler.sendEmptyMessageDelayed(1, time);
    }

    void crashCheck() {
        for (Ball currentBall : ballList) {
            for (Ball checkBall : ballList) {
                if (currentBall.id != checkBall.id) {
                    if (currentBall.isCrash(checkBall)) {
                        if (checkBall.special > 0){
                            currentBall.clickSpecial = checkBall.special;
                        }else{
                            currentBall.clickSpecial = 5;
                        }
                        currentBall.crashChanged(checkBall);
                    }
                }
            }
        }
        for (int i = ballList.size() - 1; i>= 0; i--){
            Ball ball = ballList.get(i);
            if (ball.clickSpecial == 10){
                if (ball.special <= 0){
                    boolean out = ball.big();
                    if (out){
                        List<Ball> separateList = ball.separate();
                        if (separateList.size() > 0){
                            ballList.remove(ball);
                            ballList.addAll(separateList);
                        }
                    }
                }
            }else if (ball.clickSpecial == 20){
                if (ball.special <= 0){
                    List<Ball> separateList = ball.separate();
                    if (separateList.size() > 0){
                        ballList.remove(ball);
                        ballList.addAll(separateList);
                    }else{
                        ballList.remove(ball);
                    }
                    if (listener != null){
                        listener.onClick(this);
                    }
                }
            }else if (ball.clickSpecial == 5){
                //ball.small();
            }
        }
    }


    private View.OnClickListener listener;
    public void setOnBallSeparateListener(View.OnClickListener listener){
        this.listener = listener;
    }


    Ball touchBall;
    //VelocityTracker mVelocityTracker;
    float x, y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchBall = null;
                touchBall = getTouchBall(x, y);
                LogConsole.log("ACTION_DOWN " + touchBall);
                if (touchBall != null) {
                    ballTouchExpand = new BallTouchExpand();
                    ballTouchExpand.ball = touchBall;
//                    if (mVelocityTracker == null) {
//                        mVelocityTracker = VelocityTracker.obtain();//获得VelocityTracker类实例
//                    }
                    touchBall.onTouch();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchBall != null) {
//                    mVelocityTracker.addMovement(event);
                    touchBall.onMove(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                LogConsole.log("ACTION_CANCEL " + touchBall);
                if (touchBall != null) {
                    int index = getTouchIndex();
                    if (index < 0){
                        touchBall.onRelease(x, y);
                    }else{
                        if (index == 0){
                            removeBall(touchBall);
                        }else if(index == 1){
                            touchBall.stop();
                        }else if(index == 2){
                            touchBall.resume();
                        }else if(index == 3){
                            touchBall.ballInterpolator = BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.ACCELERATE);
                            touchBall.resume();
                        }else if(index == 4){
                            touchBall.ballInterpolator = BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.LINEAR);
                            touchBall.resume();
                        }
                    }
                    touchBall = null;
                }
                break;

        }

        return super.onTouchEvent(event);
    }

    Ball getTouchBall(float x, float y) {
        for (Ball ballModel : ballList) {
            if (ballModel.isTouched(x, y)) {
                return ballModel;
            }
        }
        return null;
    }


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (running) {
                start();
            }
        }

        ;
    };



    public int getTouchIndex(){
        if (ballTouchExpand != null){
            return ballTouchExpand.getTouchIndex();
        }
        return -1;
    }

    BallTouchExpand ballTouchExpand;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Ball ballModel : ballList)
            ballModel.draw(canvas);

        if (touchBall != null && touchBall.isTouched) {
            paint.setColor(touchBall.color);
            canvas.drawLine(touchBall.cx, touchBall.cy, x, y, paint);
            ballTouchExpand.onDraw(canvas, x, y);
        }

    }


    public void clear() {
        ballList.clear();
    }

    public void removeBall(Ball ball){
        ballList.remove(ball);
    }

    /**
     * 添加一个球
     *
     */
    public void add1RandomBall() {
        String interpolatorFlag = BallInterpolatorFactory.LINEAR;
        int special = -1;
        if (ballList.size() == 0) {
            interpolatorFlag = BallInterpolatorFactory.KEEP;
            special = 10;
        }
        if (ballList.size() == 1) {
            interpolatorFlag = BallInterpolatorFactory.KEEP;
            special = 20;
        }
        IInterpolator interpolator = BallInterpolatorFactory.getInterpolator(interpolatorFlag);
        Ball ballModel = new Ball.Builder()
                .setId(System.currentTimeMillis())
                .setEdge(getWidth(), getHeight())
                .setInterpolator(interpolator)
                .setSpecial(special)
                .build();
        ballModel.randomSetUp();
        add1Ball(ballModel);
        ballList.add(ballModel);
        if (!running){
            start();
        }
    }

    /**
     * 添加一个球
     *
     */
    public void add1Ball(Ball ball) {
        ballList.add(ball);
        if (!running){
            start();
        }
    }

}

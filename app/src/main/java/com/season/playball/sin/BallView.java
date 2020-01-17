package com.season.playball.sin;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.season.playball.LogConsole;
import com.season.playball.sin.interpolator.BallInterpolatorFactory;
import com.season.playball.sin.interpolator.IInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
        init2();
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
            if (ball.clickSpecial > 0){
                if (ball.special <= 0){
                    setTag(ball.clickSpecial);
                    ballList.remove(ball);
                    if (listener != null){
                        listener.onClick(this);
                    }
                }
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

    public int getTopHeight(){
        return getHeight() * 3/4;
    }

    boolean isBottomTouched = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                if (y >= getTopHeight()){
                    isBottomTouched = true;
                    return true;
                }else{
                    isBottomTouched = false;
                }
                touchBall = null;
                touchBall = getTouchBall(x, y);
                LogConsole.log("ACTION_DOWN " + touchBall);
                if (touchBall != null) {
                    ballTouchExpand = new BallTouchExpand();
                    ballTouchExpand.ball = touchBall;
                    ballTouchExpand.setMaxRadius(touchBall.getRadius()/2);
                    touchBall.onTouch();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isBottomTouched){
                    setProgress((event.getX() - mTouchStartX) * SLIPPAGE_FACTOR,
                            (event.getY() - mTouchStartY) * SLIPPAGE_FACTOR, true);
                }else if (touchBall != null) {
//                    mVelocityTracker.addMovement(event);
                    touchBall.onMove(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isBottomTouched){
                    release();
                }else{
                    LogConsole.log("ACTION_CANCEL " + touchBall);
                    if (touchBall != null) {
                        int index = getTouchIndex();
                        if (index < 0){
                            touchBall.onRelease(x, y);
                        }else{
                            if (index == 0){
                                if (listener != null){
                                    setTag(touchBall.special);
                                    listener.onClick(this);
                                }
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
                }
                break;

        }

        return super.onTouchEvent(event);
    }

    Paint bgPaint = new Paint();
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

        canvas.drawRect(0, getTopHeight() , getWidth(), getHeight(), bgPaint);
        if (isBottomTouched){
            canvas.drawPath(mPath, mPathPaint);
            canvas.drawCircle(mCirclePointX, mCirclePointY, mCircleRadius, mCirclePaint);
         //   LogConsole.log("onDraw "+ mCirclePointX +","+ mCirclePointY + ">>>"+ mCircleRadius);
        }else {
            canvas.drawCircle(mCirclePointX, mCirclePointY, mCircleRadius, mCirclePaint);
        }
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
    public void add1Ball(Ball ball) {
        ballList.add(ball);
        if (!running){
            start();
        }
    }

    /**
     * 设置进度
     *
     */
    public void setProgress(float px, float py, boolean bg) {
        mProgressX = px;
        mProgressY = py;
        updatePathLayout(bg);
        invalidate();
    }

    private float recordProgressX, recordProgressY;
    /**
     * 添加释放动作
     */
    public void release() {
        recordProgressX = mProgressX;
        recordProgressY = mProgressY;
        if (valueAnimator == null) {
            ValueAnimator animator = ValueAnimator.ofFloat(mProgressY, 0f);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float) {
                        float newProgress = (float) val;
                        if (newProgress == 0){
                            isBottomTouched = false;
                            if (listener2 != null){
                                listener2.onClick(BallView.this);
                            }
                            mCircleRadius = 6;
                            int color = 0xff000000 | new Random().nextInt(0x00ffffff);
                            mCirclePaint.setColor(color);
                            mCirclePointX = getWidth()/2;
                            mCirclePointY = getTopHeight()  - mCircleRadius;
                            invalidate();
                        }else{
                            setProgress(newProgress * recordProgressX / recordProgressY, newProgress, false);
                        }
                    }
                }
            });
            valueAnimator = animator;
        } else {
            valueAnimator.cancel();
            valueAnimator.setFloatValues(mProgressY, 0f);
        }
        valueAnimator.start();
    }


    public float getDegree() {
        double degree = Math.atan2(recordProgressY, recordProgressX);
        degree = 180 * degree / Math.PI;
        return (float) degree + 180;
    }

    private Paint mCirclePaint;//圆的画笔
    private int mCircleRadius = 50;//圆的半径
    private float mCirclePointX;//圆的xy坐标
    private float mCirclePointY;
    private float mProgressY, mProgressX;//进度
    private Path mPath = new Path();//贝塞尔曲线
    private Paint mPathPaint;
    private ValueAnimator valueAnimator;//释放动画


    private void init2() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);//抗锯齿
        paint.setDither(true);//防抖动
        paint.setStyle(Paint.Style.FILL);//填充方式
        int color = 0xff000000 | new Random().nextInt(0x00ffffff);
        paint.setColor(color);
        mCirclePaint = paint;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//初始化路径部分画笔
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL); ;
        paint.setColor(Color.WHITE);
        mPathPaint = paint;

        bgPaint.setColor(0xAAC9C9C9);
        mCircleRadius = 6;
    }

    private void updatePathLayout(boolean bigger) {
        if (bigger){
            mCircleRadius = (int) Math.sqrt(mProgressX * mProgressX + mProgressY * mProgressY)/4;
        }
        LogConsole.log("updatePathLayout : "+ mProgressX+", "+mProgressY);
        LogConsole.log("updatePathLayout : "+ mCircleRadius);
        mCircleRadius = Math.min(80, mCircleRadius);
        mCirclePointX = getWidth()/2 + mProgressX;
        mCirclePointY = getTopHeight() + mProgressY - mCircleRadius;
        //重置
        mPath.reset();
        mPath.moveTo(0, getTopHeight());
        //左边贝塞尔曲线
        mPath.quadTo(getWidth()/2, getTopHeight(), mCirclePointX , mCirclePointY );
        //连接到右边
        mPath.lineTo(mCirclePointX , mCirclePointY );
        //右边贝塞尔曲线
        mPath.quadTo(getWidth()/2, getTopHeight(), getWidth(), getTopHeight());

    }

    private float mTouchStartY, mTouchStartX;
    private static final float SLIPPAGE_FACTOR = 0.8f;// 拖动阻力因子 0~1


    public int getRadius() {
        return mCircleRadius;
    }

    public int getColor() {
        return mCirclePaint.getColor();
    }

    public int getSpeed() {
        return (int) ((recordProgressX * recordProgressX + recordProgressY * recordProgressY) * 32
                /(getWidth()*getWidth()/4 + getHeight() * getHeight()/8));
    }

    private View.OnClickListener listener2;
    public void setOnBallAddListener(View.OnClickListener listener){
        this.listener2 = listener;
    }


}

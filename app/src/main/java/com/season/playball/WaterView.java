package com.season.playball;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * Created by shenhua on 2017/10/21.
 * Email shenhuanet@126.com
 */
public class WaterView extends View {

    private Paint mCirclePaint;//圆的画笔
    private int mCircleRadius = 50;//圆的半径
    private float mCirclePointX;//圆的xy坐标
    private float mCirclePointY;
    private float mProgressY, mProgressX;//进度
    private Path mPath = new Path();//贝塞尔曲线
    private Paint mPathPaint;
    private ValueAnimator valueAnimator;//释放动画

    public WaterView(Context context) {
        this(context, null);
    }

    public WaterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
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

        updatePathLayout();
    }

    private void updatePathLayout() {
        LogConsole.log("updatePathLayout");
        mCirclePointX = getWidth()/2 + mProgressX;
        mCirclePointY = mProgressY - mCircleRadius;

        //重置
        mPath.reset();
        mPath.moveTo(0, 0);
        //左边贝塞尔曲线
        mPath.quadTo(getWidth()/2, 0, mCirclePointX , mCirclePointY );
        //连接到右边
        mPath.lineTo(mCirclePointX , mCirclePointY );
        //右边贝塞尔曲线
        mPath.quadTo(getWidth()/2, 0, getWidth(), 0);

    }

    private float mTouchStartY, mTouchStartX;
    private static final float SLIPPAGE_FACTOR = 0.8f;// 拖动阻力因子 0~1

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                setProgress((event.getX() - mTouchStartX) * SLIPPAGE_FACTOR,
                        (event.getY() - mTouchStartY) * SLIPPAGE_FACTOR);
                return true;
            case MotionEvent.ACTION_UP:
                release();
                return true;
            default:
                break;

        }
        return false;
    }

    public float getDegree() {
        double degree = Math.atan2(recordProgressY, recordProgressX);
        degree = 180 * degree / Math.PI;
        return (float) degree + 180;
    }

    /**
     * 获取当前值
     *
     * @param start    起点
     * @param end      终点
     * @param progress 进度
     * @return 某一个坐标差值的百分百，计算贝塞尔的关键
     */
    private float getValueByLine(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPathPaint);
        //画圆
        canvas.drawCircle(mCirclePointX, mCirclePointY, mCircleRadius, mCirclePaint);
        LogConsole.log("onDraw "+ mCirclePointX +","+ mCirclePointY + ">>>"+ mCircleRadius);
    }

    /**
     * 设置进度
     *
     */
    public void setProgress(float px, float py) {
        mProgressX = px;
        mProgressY = py;
        updatePathLayout();
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
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float) {
                        float newProgress = (float) val;
                      //  setProgress(newProgress, newProgress * recordProgressY / recordProgressX);
                        setProgress(newProgress * recordProgressX / recordProgressY, newProgress);
                        if (newProgress == 0 && listener != null){
                            listener.onClick(WaterView.this);
                            int color = 0xff000000 | new Random().nextInt(0x00ffffff);
                            mCirclePaint.setColor(color);
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

    private View.OnClickListener listener;
    public void setOnBallAddListener(View.OnClickListener listener){
        this.listener = listener;
    }

    public int getColor() {
        return mCirclePaint.getColor();
    }

    public int getSpeed() {
        return (int) ((recordProgressX * recordProgressX + recordProgressY * recordProgressY) * 32
                        /(getWidth()*getWidth()/4 + getHeight() * getHeight()));
    }
}
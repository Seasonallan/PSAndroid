package com.season.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.season.library.R;


/**
 * 58同城 加载动画
 */
public class LoadingView extends View {

    private static final float FACTOR = 1.2f;
    private static float mDistance = 186;

    private int mDelay;
    private LoadingViewTopShape mShapeLoadingView;


    public LoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private AccelerateInterpolator upInterpolator;
    private DecelerateInterpolator downInterpolator;
    private String loadingText;
    private TextPaint textPaint;

    private void init(Context context, AttributeSet attrs) {

        mShapeLoadingView = new LoadingViewTopShape(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        String loadText = typedArray.getString(R.styleable.LoadingView_loadingText);
        mDelay = typedArray.getInteger(R.styleable.LoadingView_delay, 500);
        typedArray.recycle();

        textPaint = new TextPaint();
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(32);
        textPaint.setAntiAlias(true);
        upInterpolator = new AccelerateInterpolator(FACTOR);
        downInterpolator = new DecelerateInterpolator(FACTOR);

        currentTime = System.currentTimeMillis();
        setLoadingText(loadText);
    }

    float textWidth;
    public void setLoadingText(CharSequence textChar) {
        if (TextUtils.isEmpty(textChar)){
            this.loadingText = "正在加载中";
        }else{
            this.loadingText = textChar.toString();
        }
        textWidth = textPaint.measureText(loadingText);
        invalidate();
    }

    long currentTime;
    boolean isUp;

    @Override
    protected void onDraw(Canvas canvas) {
        drawContent(canvas);
        invalidate();
    }

    protected int getLoadingWidth(){
        return getWidth();
    }

    protected int getLoadingHeight(){
        return getHeight();
    }

    RectF rect = new RectF();
    int radius = 4;
    public void drawContent(Canvas canvas) {
        if (getLoadingWidth() <= 0) {
            return;
        }
        textPaint.setColor(mShapeLoadingView.getColor());

        canvas.drawText(loadingText, getLoadingWidth() / 2 - textWidth / 2,
                getLoadingHeight() / 2 + mDistance/2 +  mShapeLoadingView.getWidthHeight() * 2 + radius * 15, textPaint);

        int time = (int) (System.currentTimeMillis() - currentTime);
        if (time > mDelay) {
            isUp = !isUp;
            time = 0;
            if (!isUp) {
                mShapeLoadingView.changeShape();
            }
            currentTime = System.currentTimeMillis();
        }

        float dx = getLoadingWidth() / 2 - mShapeLoadingView.getWidthHeight() / 2;
        float dy, percent;
        if (isUp) {
            percent = upInterpolator.getInterpolation(time * 1.0f / mDelay);
            dy = getLoadingHeight() / 2 - mDistance/2 + percent * mDistance;
        } else {
            percent = downInterpolator.getInterpolation(time * 1.0f / mDelay);
            dy = getLoadingHeight() / 2 + mDistance/2 - percent * mDistance;
        }

        float width = isUp? percent * textWidth/2 : (1 - percent) * textWidth/2;
        rect.left = getLoadingWidth()/2 - width;
        rect.right = getLoadingWidth()/2 + width;
        rect.top = getLoadingHeight() / 2 + mDistance/2 +  mShapeLoadingView.getWidthHeight() * 2 - radius;
        rect.bottom = getLoadingHeight() / 2 + mDistance/2 +  mShapeLoadingView.getWidthHeight() * 2 + radius;
        canvas.drawRoundRect(rect, radius, radius, textPaint);

        if (isUp) {
            canvas.translate(dx, dy);
        } else {
            canvas.translate(dx, dy);
            canvas.rotate(360 * percent,
                    mShapeLoadingView.getWidthHeight()/2, mShapeLoadingView.getWidthHeight()/2);
        }
        mShapeLoadingView.drawContent(canvas);
    }

}

package com.season.lib.ps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class VerticalSeekBar extends View {

    public interface OnSeekBarChangeListener {
        void onProgressChanged(int progress);
    }
    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public VerticalSeekBar(Context context) {
        this(context, null);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    private int progress = 50;
    private Paint paint;
    protected void onDraw(Canvas c) {
        if (paint == null){
            paint = new Paint();
            paint.setColor(0xff9ad3ff);
        }
        paint.setColor(0xffffffff);
        c.drawRect(0, 0,getWidth(), getHeight(), paint);
        paint.setColor(0xff9ad3ff);
        c.drawRect(0, getPercent(),getWidth(), getHeight(), paint);
        super.onDraw(c);
    }

    private int getPercent() {
        return progress;
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                progress = (int) event.getY();
                invalidate();
                if (mOnSeekBarChangeListener != null){
                    mOnSeekBarChangeListener.onProgressChanged((getHeight() - progress) * 100/getHeight());
                }
                break;
        }
        return true;
    }


}

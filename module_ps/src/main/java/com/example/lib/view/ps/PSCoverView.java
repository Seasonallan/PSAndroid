package com.example.lib.view.ps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 布局最外层View， 用于拦截第一个事件，
 * 用于选择背景展开的时候 点击屏幕其他的位置关闭背景的radioGroup
 * Created by Administrator on 2017/10/27.
 */

public class PSCoverView extends FrameLayout {
    public PSCoverView(@NonNull Context context) {
        super(context);
    }

    public PSCoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PSCoverView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnUpListener listener;
    public void setOnActionUpListener(OnUpListener listener){
        this.listener = listener;
    }

    public interface OnUpListener {
        boolean onActionUp(float x, float y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();
            if (listener != null){
                try {
                    listener.onActionUp(x, y);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return super.onTouchEvent(event);
    }
}

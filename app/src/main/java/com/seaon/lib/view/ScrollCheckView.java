package com.seaon.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Administrator on 2017/10/30.
 */

public class ScrollCheckView extends RelativeLayout{
    public ScrollCheckView(@NonNull Context context) {
        super(context);
    }

    public ScrollCheckView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollCheckView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    boolean active = false;
    float x,  y;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                active = true;
                x = ev.getX();
                y = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - x;
                float dy = ev.getY() - y;
                if (dy < -80 && active){
                    if (Math.abs(dy) > Math.abs(dx)){
                        if (listener != null){
                            listener.onScrollUp();
                            active = false;
                        }
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface OnScrollListener{
        void onScrollUp();
    }

    private OnScrollListener listener;
    public void setOnScrollListener(OnScrollListener listener){
        this.listener = listener;
    }
}

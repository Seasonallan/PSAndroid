package com.season.lib.view.ps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class CustomBaseView extends View implements ILayer {


    public CustomBaseView(Context context) {
        super(context);
    }

    public CustomBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int startTime = -1;
    public int endTime = -1;

    @Override
    public int getStartTime() {
        if (startTime < 0){
            return 0;
        }
        return startTime;
    }

    @Override
    public int getEndTime() {
        if (endTime < 0){
            return getDuration();
        }
        return endTime;
    }


    @Override
    public boolean setStartTime(int time) {
        startTime = time;
        return true;
    }

    @Override
    public boolean setEndTime(int time) {
        endTime = time;
        return true;
    }

}

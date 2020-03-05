package com.season.example.layout;

import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ps.R;
import com.season.lib.util.LogUtil;
import com.season.lib.view.ps.ILayer;
import com.season.lib.view.ps.PSCanvas;


public class BottomVipLayout {

    private View containerView;
    private Activity activity;
    private <T extends View> T findView(int id){
        return activity.findViewById(id);
    }
    public BottomVipLayout(Activity activity) {
        this.activity = activity;
        containerView = findView(R.id.layout_vip);
        containerView.setVisibility(View.GONE);


        findView(R.id.tc_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mTotalTime = findView(R.id.vip_total_time);
        mCurrentTime = findView(R.id.vip_current_time);
        mStartTime = findView(R.id.vip_start_time);
        mEndTime = findView(R.id.vip_end_time);
        mStartSeek = findView(R.id.vip_sb_start);
        mEndSeek = findView(R.id.vip_sb_end);
        findView(R.id.vip_btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mStartSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!seeking){
                    return;
                }
                if (progress > startMaxDuration){
                    seekBar.setProgress(startMaxDuration);
                    return;
                }
                if (currentView == null){
                    return;
                }
                mStartTime.setText("开始时间:"+ progress +"ms");
                currentView.setStartTime(progress);
                int endProgress = mEndSeek.getProgress();
                int duration = currentView.getDuration();
                if (duration > 0){
                    mEndSeek.setProgress(progress + duration);
                }else{
                    if (endProgress < progress){
                        mEndSeek.setProgress(progress);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking = false;
            }
        });
        mEndSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!seeking){
                    return;
                }
                if (progress < endMinDuration){
                    seekBar.setProgress(endMinDuration);
                    return;
                }
                if (currentView == null){
                    return;
                }
                mEndTime.setText("结束时间:"+ progress +"ms");
                currentView.setEndTime(progress);
                int startProgress = mStartSeek.getProgress();
                int duration = currentView.getDuration();
                if (duration > 0){
                    mStartSeek.setProgress(progress - duration);
                }else{
                    if (startProgress > progress){
                        mStartSeek.setProgress(progress);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seeking = false;
            }
        });
    }
    private boolean seeking = false;
    private TextView mTotalTime, mCurrentTime, mStartTime, mEndTime;
    private SeekBar mStartSeek, mEndSeek;


    public boolean isShowing(){
        return containerView.getVisibility() == View.VISIBLE;
    }

    public void show() {
        containerView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        containerView.setVisibility(View.GONE);
    }


    ILayer currentView;
    int totalDuration = 0;
    int startMaxDuration = 0;
    int endMinDuration = 0;
    public void statusChange(PSCanvas psCanvas) {
        View view = psCanvas.getFocusView();
        totalDuration = 0;
        startMaxDuration = 0;
        endMinDuration = 0;
        if (view != null && view instanceof ILayer){
            currentView = (ILayer) view;
            totalDuration = psCanvas.maxDuration;
            startMaxDuration = totalDuration - currentView.getDuration();
            endMinDuration = currentView.getDuration();
            LogUtil.e(""+ currentView.getStartTime() + ", "+ currentView.getEndTime());
            mTotalTime.setText("画布时长:"+ totalDuration +"ms");
            mStartSeek.setMax(totalDuration);
            mEndSeek.setMax(totalDuration);
            mCurrentTime.setText("图层时长:"+ currentView.getDuration()+"ms");
            mStartTime.setText("开始时间:"+ currentView.getStartTime()+"ms");
            mEndTime.setText("结束时间:"+ currentView.getEndTime()+"ms");
            mStartSeek.setProgress(currentView.getStartTime());
            mEndSeek.setProgress(currentView.getEndTime());
        }
    }
}

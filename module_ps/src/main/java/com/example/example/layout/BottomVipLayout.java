package com.example.example.layout;

import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ps.R;
import com.example.lib.view.ps.ILayer;
import com.example.lib.view.ps.PSCanvas;



public class BottomVipLayout extends BaseBottomView{


    @Override
    protected int getContentId() {
        return R.id.layout_vip;
    }

    public BottomVipLayout(Activity activity) {
        super(activity);

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
        mRepeatSeek = findView(R.id.vip_sb_count);

        mStartSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!seeking){
                    return;
                }
                if (progress > getStartMaxProgress()){
                    seekBar.setProgress(getStartMaxProgress());
                    return;
                }
                if (currentView == null){
                    return;
                }
                mStartTime.setText("开始时间:"+ progress +"ms");
                currentView.setStartTime(progress);
                int endProgress = mEndSeek.getProgress();
                if (duration > 0){
                    if (mEndSeek.getProgress() < progress + duration * (repeatCount + 1)){
                        mEndSeek.setProgress(progress + duration * (repeatCount + 1));
                    }
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
                if (progress < getEndMinProgress()){
                    seekBar.setProgress(getEndMinProgress());
                    return;
                }
                if (currentView == null){
                    return;
                }
                mEndTime.setText("结束时间:"+ progress +"ms");
                currentView.setEndTime(progress);
                int startProgress = mStartSeek.getProgress();
                if (duration > 0){
                    if (mStartSeek.getProgress() > progress - duration*(repeatCount + 1)){
                        mStartSeek.setProgress(progress - duration*(repeatCount + 1));
                    }
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
        mRepeatSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!seekingRepeat){
                    return;
                }
                if (duration > 0){
                    repeatCount = progress;
                    mStartSeek.setProgress(0);
                    mStartTime.setText("开始时间:"+ 0 +"ms");
                    currentView.setStartTime(0);

                    int end = duration * (repeatCount + 1);
                    mEndSeek.setProgress(end);
                    mEndTime.setText("结束时间:"+ end +"ms");
                    currentView.setEndTime(end);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekingRepeat = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekingRepeat = false;
            }
        });
    }
    private boolean seeking = false, seekingRepeat = false;
    private TextView mTotalTime, mCurrentTime, mStartTime, mEndTime;
    private SeekBar mStartSeek, mEndSeek, mRepeatSeek;


    int getStartMaxProgress(){
        if (duration > 0){
            return totalDuration - duration * (repeatCount + 1);
        }else{
            return totalDuration;
        }
    }
    int getEndMinProgress(){
        if (duration > 0){
            return duration * (repeatCount + 1);
        }else{
            return 0;
        }
    }

    ILayer currentView;
    int totalDuration = 0;
    int duration = 0;

    int repeatCount = Integer.MAX_VALUE;
    int maxRepeatCount = 1;
    public void statusChange(PSCanvas psCanvas) {
        View view = psCanvas.getFocusView();
        totalDuration = 0;
        duration = 0;
        repeatCount = 0; maxRepeatCount = 0;
        if (view != null && view instanceof ILayer){
            currentView = (ILayer) view;
            totalDuration = psCanvas.maxDuration;
            duration = currentView.getDuration();
            if (currentView.isRepeat()){
                if (duration > 0){
                    repeatCount = (currentView.getEndTime() - currentView.getStartTime())/ duration - 1;
                    maxRepeatCount = totalDuration/ duration - 1;
                }
            }else{
                repeatCount = 0;
                maxRepeatCount = 0;
            }

           // LogUtil.e(""+ currentView.getStartTime() + ", "+ currentView.getEndTime());
           // LogUtil.e("-->>>"+ maxRepeatCount + ", "+ repeatCount);
            mTotalTime.setText("画布时长:"+ totalDuration +"ms");
            mCurrentTime.setText("图层时长:"+ duration +"ms");
            mStartTime.setText("开始时间:"+ currentView.getStartTime()+"ms");
            mEndTime.setText("结束时间:"+ currentView.getEndTime()+"ms");
            mStartSeek.setMax(totalDuration);
            mEndSeek.setMax(totalDuration);
            mStartSeek.setProgress(currentView.getStartTime());
            mEndSeek.setProgress(currentView.getEndTime());
            mRepeatSeek.setMax(maxRepeatCount);
            mRepeatSeek.setProgress(repeatCount);
        }
    }
}

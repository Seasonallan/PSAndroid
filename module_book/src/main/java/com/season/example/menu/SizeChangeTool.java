package com.season.example.menu;

import android.view.View;
import android.widget.TextView;
import com.example.book.R;
import com.season.lib.util.ToastUtil;

public abstract class SizeChangeTool {
    private TextView mSizeView;
    private View mCutBut;
    private View mAddBut;
    private View mParentView;

    public abstract int getLevel();
    public abstract void setLevel(int level);
    public abstract int getValue();
    public abstract String getDesc();

    public SizeChangeTool(View parentView){
        this.mParentView = parentView;
        mCutBut = mParentView.findViewById(R.id.menu_settings_sut_but);
        TextView descView = mParentView.findViewById(R.id.menu_settings_desc);
        descView.setText(getDesc());
        mAddBut = mParentView.findViewById(R.id.menu_settings_add_but);
        mSizeView = mParentView.findViewById(R.id.menu_settings_size);
        mAddBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temtFontProgress = getLevel();
                temtFontProgress += 1;
                if(temtFontProgress > getLevelCount()){
                    temtFontProgress = getLevelCount();
                }
                if(temtFontProgress == getLevelCount()){
                    ToastUtil.showToast("当前为最大"+getDesc());
                    v.setEnabled(false);
                }
                setLevel(temtFontProgress);
                mSizeView.setText(getValue()+"");
                if(!mCutBut.isEnabled()){
                    mCutBut.setEnabled(true);
                }
            }
        });
        mCutBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temtFontProgress = getLevel();
                temtFontProgress -= 1;
                if(temtFontProgress < 0){
                    ToastUtil.showToast("当前为最小"+getDesc());
                    temtFontProgress = 0;
                }
                if(temtFontProgress == 0){
                    v.setEnabled(false);
                }
                setLevel(temtFontProgress);
                mSizeView.setText(getValue()+"");
                if(!mAddBut.isEnabled()){
                    mAddBut.setEnabled(true);
                }
            }
        });
    }

    public void resetStatus() {
        //同步字体大小设置状态
        int temtFontProgress = getLevel();
        if(temtFontProgress == getLevelCount()){
            mAddBut.setEnabled(false);
        }else if(temtFontProgress == 0){
            mCutBut.setEnabled(false);
        }
        mSizeView.setText(getValue()+"");

    }

    public abstract int getLevelCount();


}

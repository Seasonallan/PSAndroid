package com.season.example.menu;

import android.view.View;
import android.widget.TextView;
import com.season.book.R;
import com.season.lib.util.ToastUtil;

public abstract class SizeChangeTool {
    private TextView mSizeView;
    private TextView mCutBut;
    private TextView mAddBut;
    private View mParentView;

    public abstract int getLevel();
    public abstract void setLevel(int level);
    public abstract int getValue();
    public abstract String getDesc();

    public void setText(String left, String right){
        mCutBut.setText(left);
        mAddBut.setText(right);
    }

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
                if (getLevelCount() > 1){
                    temtFontProgress += 1;
                    if(temtFontProgress > getLevelCount()){
                        temtFontProgress = getLevelCount();
                    }
                    if(temtFontProgress == getLevelCount()){
                        ToastUtil.showToast("当前为最大"+getDesc());
                        v.setEnabled(false);
                    }
                }else{
                    temtFontProgress = 1;
                }
                setLevel(temtFontProgress);
                resetStatus();
            }
        });
        mCutBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temtFontProgress = getLevel();
                if (getLevelCount() > 1){
                    temtFontProgress -= 1;
                    if(temtFontProgress < 0){
                        ToastUtil.showToast("当前为最小"+getDesc());
                        temtFontProgress = 0;
                    }
                    if(temtFontProgress == 0){
                        v.setEnabled(false);
                    }
                }else{
                    temtFontProgress = 0;
                }
                setLevel(temtFontProgress);
                resetStatus();
            }
        });
    }

    public void resetStatus() {
        //同步字体大小设置状态
        int temtFontProgress = getLevel();
        if (getLevelCount() > 1){
            mAddBut.setEnabled(temtFontProgress != getLevelCount());
            mCutBut.setEnabled(temtFontProgress != 0);
            mSizeView.setText(getValue()+"");
        }else{
            mCutBut.setSelected(temtFontProgress == 0);
            mAddBut.setSelected(temtFontProgress == 1);
        }
    }

    public abstract int getLevelCount();


}

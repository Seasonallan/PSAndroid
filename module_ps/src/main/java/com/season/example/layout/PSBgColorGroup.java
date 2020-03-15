package com.season.example.layout;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.season.ps.R;
import com.season.lib.util.LogUtil;
import com.season.ps.view.ColorPickView;
import com.season.ps.view.ps.PSBackground;
import com.season.ps.view.ps.PSCanvas;

/**
 * 底部背景选择器
 */
public abstract class PSBgColorGroup implements View.OnClickListener {

    ColorPickView rb_vid_pic;
    Activity mActivity;
    RadioGroup rgAutoBg;
    ImageView vColor;
    View iv_lable;
    PSCanvas mPsCanvas;

    public PSBgColorGroup(Activity activity, PSCanvas canvas){
        this.mActivity = activity;
        this.mPsCanvas = canvas;

        rb_vid_pic = mActivity.findViewById(R.id.rb_vid_pic);
        rgAutoBg = mActivity.findViewById(R.id.rg_auto_bg);
        vColor = mActivity.findViewById(R.id.v_color);
        iv_lable = mActivity.findViewById(R.id.iv_lable);


        vColor.setOnClickListener(this);
        iv_lable.setOnClickListener(this);

        rgAutoBg.getLayoutParams().height = 47;
        rgAutoBg.requestLayout();
        //改背景
        rgAutoBg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setCanvaBackGround(checkedId);
                onBgChanged();
            }
        });
    }

    public abstract void onBgChanged();

    public void setCanvaBackGround(int i) {
        if (i == R.id.rb_white) {
            vColor.setImageResource(R.mipmap.icon_color_white);
            //setBrushColor(Color.WHITE, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mPsCanvas.showBackground(Color.WHITE);
            }

        } else if (i == R.id.rb_translate) {
            vColor.setImageResource(R.mipmap.icon_color_transparent);
            //setBrushColor(Color.TRANSPARENT, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mPsCanvas.showBackground(Color.TRANSPARENT);
            }

        } else if (i == R.id.rb_hui) {
            vColor.setImageResource(R.mipmap.icon_color_gray);
            //setBrushColor(Color.GRAY, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mPsCanvas.showBackground(Color.GRAY);
            }

        } else if (i == R.id.rb_black) {
            vColor.setImageResource(R.mipmap.icon_color_black);
            //setBrushColor(Color.BLACK, false);
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mPsCanvas.showBackground(Color.BLACK);
            }

        } else if (i == R.id.rb_vid_pic) {
            if (rgAutoBg.getVisibility() == View.VISIBLE) {
                mPsCanvas.showVideoOrImage();
            }
            if (mPsCanvas.backgroundView.currentOperate != null) {
                if (mPsCanvas.backgroundView.currentOperate.bitmap != null) {
                    rb_vid_pic.setDrawImage(mPsCanvas.backgroundView.currentOperate.bitmap);
                    vColor.setImageBitmap(mPsCanvas.backgroundView.currentOperate.bitmap);
                    rb_vid_pic.setVisibility(View.VISIBLE);
                }
            }
        }
        vColor.setVisibility(View.VISIBLE);
        rgAutoBg.setVisibility(View.GONE);
    }

    public void resetRgAutoBg() {
        PSBackground.BgOperate op = mPsCanvas.backgroundView.currentOperate;
        if (op != null) {
            int checkId = R.id.rb_vid_pic;
            if (op.visible1 == View.VISIBLE) {
                int color = op.color;
                checkId = R.id.rb_translate;
                if (color == Color.WHITE) {
                    checkId = R.id.rb_white;
                } else if (color == Color.TRANSPARENT) {
                    checkId = R.id.rb_translate;
                } else if (color == Color.GRAY) {
                    checkId = R.id.rb_hui;
                } else if (color == Color.BLACK) {
                    checkId = R.id.rb_black;
                }
            } else {
                if (rgAutoBg.getCheckedRadioButtonId() == checkId) {
                    setCanvaBackGround(checkId);
                }
            }
            ((RadioButton) rgAutoBg.findViewById(checkId)).setChecked(true);
        }
    }

    public boolean closeIfOpened(float x, float y) {
        if (rgAutoBg.getVisibility() == View.VISIBLE) {
            int[] location = new int[2];
            rgAutoBg.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + rgAutoBg.getMeasuredWidth();
            int bottom = top + rgAutoBg.getMeasuredHeight();
            if (y >= top && y <= bottom && x >= 0 && x <= right) {
                LogUtil.LOG("N ");
            } else {
                vColor.setVisibility(View.VISIBLE);
                rgAutoBg.setVisibility(View.GONE);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (rgAutoBg.getVisibility() == View.VISIBLE) {
            rgAutoBg.setVisibility(View.GONE);
            vColor.setVisibility(View.VISIBLE);
        } else {
            rgAutoBg.setVisibility(View.VISIBLE);
            for (int ci = 0;ci < rgAutoBg.getChildCount();ci++){
                rgAutoBg.getChildAt(ci).invalidate();
            }
            vColor.setVisibility(View.GONE);
        }
    }
}

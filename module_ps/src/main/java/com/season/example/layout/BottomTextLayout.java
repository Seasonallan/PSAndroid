package com.season.example.layout;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.season.ps.R;
import com.season.ps.animation.AnimationProvider;

import java.util.ArrayList;

/**
 * 字体动画
 */
public abstract class BottomTextLayout extends BaseBottomView{

    @Override
    protected int getContentId() {
        return R.id.layout_text;
    }


    private LinearLayout topLinear, bottomLinear;
    public BottomTextLayout(Activity activity) {
        super(activity);
        findView(R.id.text_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        topLinear = findView(R.id.text_l1);
        bottomLinear =  findView(R.id.text_l2);

        relativeLayouts = new ArrayList<>();
        for (int i = 0; i < AnimationProvider.strsTextShow.length/2; i++){
            addItem(topLinear, AnimationProvider.strsTextShow[i]);
        }
        for (int i = AnimationProvider.strsTextShow.length/2; i < AnimationProvider.strsTextShow.length; i++){
            addItem(bottomLinear, AnimationProvider.strsTextShow[i]);
        }
    }

    ArrayList<RelativeLayout> relativeLayouts;
    private void addItem(LinearLayout linearLayout, String descString){
        RelativeLayout relativeLayout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        itemParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        TextView textView = new TextView(activity);
        textView.setText(descString);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        relativeLayout.addView(textView, itemParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        params.setMargins(6,6,6,6);
        relativeLayout.setPadding(6,6,6,6);
        linearLayout.addView(relativeLayout, params);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick((String) v.getTag(), true);
            }
        });
        relativeLayout.setTag(descString);
        relativeLayouts.add(relativeLayout);
    }

    //选中某个动画
    public void select(int position) {
        if (position < 0 || position > AnimationProvider.strsTextShow.length - 1) {
            position = 0;
        }
        onItemClick(AnimationProvider.strsTextShow[position], false);
    }

    void onItemClick(String idSelect, boolean notify) {
        int position = 0;
        int i = 0;
        for (RelativeLayout view : relativeLayouts) {
            String tag = (String) view.getTag();
            if (tag == idSelect) {
                position = i;
                view.setBackgroundResource(R.mipmap.img_rect_sel);
            }else{
                view.setBackgroundResource(R.mipmap.img_rect);
            }
            i ++;
        }
        if (notify)
            onItemClick(position);
    }

    public abstract void onItemClick(int position);

}

package com.season.example.layout;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.example.ps.R;
import com.season.lib.animation.AnimationProvider;

/**
 * 字体动画
 */
public abstract class BottomTextLayout{


    private Activity activity;
    private View findView(int id){
        return activity.findViewById(id);
    }
    private View containerView;
    public BottomTextLayout(Activity activity) {
        this.activity = activity;
        initView();
        containerView = findView(R.id.layout_text);
        containerView.setVisibility(View.GONE);
        findView(R.id.text_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public boolean isShowing(){
        return containerView.getVisibility() == View.VISIBLE;
    }

    public void show() {
        containerView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        containerView.setVisibility(View.GONE);
    }

    int[] ids = {R.id.animation_1, R.id.animation_2, R.id.animation_3, R.id.animation_4, R.id.animation_5, R.id.animation_6, R.id
            .animation_7, R.id.animation_8, R.id.animation_9, R.id.animation_10, R.id.animation_11, R.id.animation_12, R.id
            .animation_singleline1, R.id.animation_singleline2, R.id.animation_13, R.id.animation_14};

    int[] idsText = {R.id.animation_text1, R.id.animation_text2, R.id.animation_text3, R.id.animation_text4, R.id.animation_text5, R
            .id.animation_text6, R.id.animation_text7, R.id.animation_text8, R.id.animation_text9, R.id.animation_text10, R.id
            .animation_text11, R.id.animation_text12, R.id.animation_animation_singleline1_text, R.id
            .animation_animation_singleline1_text2, R.id.animation_text13, R.id.animation_text14};

    private void initView() {
        for (int id : ids) {
            View view = findView(id);
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onItemClick(v.getId(), true);
                }
            });
        }
        int i = 0;
        for (int id : idsText) {
            TextView textView = (TextView) findView(id);
            textView.setText(AnimationProvider.strsTextShow[i]);
            i++;
        }
        //更新一个图片资源
        onItemClick(ids[0], false);
    }

    //选中某个动画
    public void select(int position) {
        if (position < 0 || position > ids.length - 1) {
            position = 0;
        }
        onItemClick(ids[position], false);
    }

    void onItemClick(int idSelect, boolean notify) {
        int position = 0;
        int i = 0;
        for (int id : ids) {
            View view = findView(id);
            if (id == idSelect) {
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

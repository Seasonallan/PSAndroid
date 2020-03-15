package com.season.example.layout;

import android.app.Activity;
import android.view.View;

import com.season.ps.R;


public abstract class BottomTucengLayout extends BaseBottomView{

    @Override
    protected int getContentId() {
        return R.id.layout_tuceng;
    }

    public BottomTucengLayout(Activity activity) {
        super(activity);

        findView(R.id.tc_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCopy();
            }
        });
        upView = findView(R.id.tc_up_img);
        downView = findView(R.id.tc_down_img);
        upViewText = findView(R.id.tc_up_text);
        downViewText = findView(R.id.tc_down_text);
        findView(R.id.tc_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpLayer();
            }
        });
        findView(R.id.tc_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownLayer();
            }
        });
    }
    private View upView, downView;
    private View upViewText, downViewText;

    public abstract void onCopy();
    public abstract void onUpLayer();
    public abstract void onDownLayer();

    public void statusChange(int viewIndex, int childCount) {
        downView.setEnabled(viewIndex != 0);
        upView.setEnabled(viewIndex < childCount - 1);
        downViewText.setEnabled(viewIndex != 0);
        upViewText.setEnabled(viewIndex < childCount - 1);
    }
}

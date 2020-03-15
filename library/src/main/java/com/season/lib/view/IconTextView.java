package com.season.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.season.library.R;

public class IconTextView extends RelativeLayout {
    private String text = "";
    private String desc = "";
    private int color = 0XFFF;
    private int icon;

    public IconTextView(Context context) {
        this(context, null);
    }

    public IconTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IconText, 0, 0);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.IconText_it_text) {
                text = ta.getString(attr);
            } else if (attr == R.styleable.IconText_it_text_color) {
                color = ta.getColor(attr, 0xffffff);
            } else if (attr == R.styleable.IconText_it_desc) {
                desc = ta.getString(attr);
            } else if (attr == R.styleable.IconText_it_icon) {
                icon = ta.getResourceId(attr, R.drawable.icon);
            }
        }
        ta.recycle();

        ImageView imageView = new ImageView(context);
        LayoutParams params = new LayoutParams(38, 38);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        imageView.setImageResource(icon);
        imageView.setId(R.id.left_icon);
        addView(imageView, params);


        TextView textView = new TextView(context);
        textView.setText(desc);
        textView.setTextColor(color);
        textView.setPadding(24,8,8,8);
        textView.setId(R.id.left_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RIGHT_OF, R.id.left_icon);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(textView, params);


        TextView contentView = new TextView(context);
        if(!TextUtils.isEmpty(desc)){
            contentView.setGravity(Gravity.CENTER);
        }
        contentView.setText(text);
        contentView.setId(R.id.right_text);
        contentView.setTextColor(color);
        contentView.setPadding(8,8,8,8);
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        // contentView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
        contentView.setTypeface(Typeface.createFromAsset(context.getAssets(), "FZSJ_XZLYJW.TTF"));
        contentView.getPaint().setFakeBoldText(true);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RIGHT_OF, R.id.left_text);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(contentView, params);


    }

}

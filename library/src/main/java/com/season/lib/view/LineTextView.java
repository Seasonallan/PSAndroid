package com.season.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.library.R;

public class LineTextView extends RelativeLayout {
    private String text = "";
    private int color = 0XFFF;

    public LineTextView(Context context) {
        this(context, null);
    }

    public LineTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LineText, 0, 0);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.LineText_text) {
                text = ta.getString(attr);
            } else if (attr == R.styleable.LineText_text_color) {
                color = ta.getColor(attr, 0xffffff);
            }
        }
        ta.recycle();

        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setPadding(8,8,8,8);
        textView.setId(R.id.center_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(textView, params);

        View lineLeft = new View(context);
        lineLeft.setBackgroundColor(color);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(LEFT_OF, R.id.center_text);
        addView(lineLeft, params);

        View lineRight = new View(context);
        lineRight.setBackgroundColor(color);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RIGHT_OF, R.id.center_text);
        addView(lineRight, params);

    }

}

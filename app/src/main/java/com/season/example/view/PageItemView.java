package com.season.example.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.season.myapplication.R;

public class PageItemView extends FrameLayout {

    Context context;
    TextView titleView, timeView, numberView;
    LinearLayout contentView;
    PageItem pageItem;

    public int getPage(){
        return pageItem.pageNumber;
    }
    public PageItemView(Context context, PageItem pageItem){
        super(context);
        this.context = context;
        this.pageItem = pageItem;
        LayoutInflater.from(context).inflate(R.layout.page_base, this);
        titleView = findViewById(R.id.page_title);
        timeView = findViewById(R.id.page_time);
        numberView = findViewById(R.id.page_number);
        contentView = findViewById(R.id.page_content);

        contentView.setBackgroundColor(pageItem.color);
        titleView.setTextColor(pageItem.color);
        timeView.setTextColor(pageItem.color);

        setTypeface(titleView, timeView);

        titleView.setText(pageItem.title);
        timeView.setText(pageItem.time);
        numberView.setText(pageItem.getPageNumber());
        if (pageItem.contentList != null){
            for (PageItem.PageContent pageContent : pageItem.contentList){
                addContentView(pageContent);
            }
        }
    }

    private void addContentView(PageItem.PageContent pageContent){
        contentView.addView(get1TextView(pageContent.title, 32, 8, 64));
        contentView.addView(get1LineView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        for (String item : pageContent.items){
            contentView.addView(get1TextView(item, 24, 76, 16));
        }
    }

    private View get1TextView(String text, int size, int paddingLeft, int paddingTop){
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(paddingLeft,paddingTop,0,0);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        setTypeface(textView);
        return textView;
    }

    private View get1LineView(){
        View lineView = new View(context);
        lineView.setBackgroundColor(0x66f1f1f1);
        return lineView;
    }

    public void setTypeface(TextView... textViews){
        for (TextView textView : textViews){
            textView.setTypeface(Typeface.createFromAsset(context.getAssets(), "FZSJ_XZLYJW.TTF"));
            textView.getPaint().setFakeBoldText(true);
        }
    }

}

package com.season.example.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.season.myapplication.R;

public class PageItemView {

    Context context;
    View containerView;
    TextView titleView, timeView, numberView;
    LinearLayout contentView;
    public PageItemView(Context context, PageItem pageItem){
        this.context = context;
        containerView = LayoutInflater.from(context).inflate(R.layout.page_base, null);
        titleView = containerView.findViewById(R.id.page_title);
        timeView = containerView.findViewById(R.id.page_time);
        numberView = containerView.findViewById(R.id.page_number);
        contentView = containerView.findViewById(R.id.page_content);

        contentView.setBackgroundColor(pageItem.color);
        titleView.setTextColor(pageItem.color);
        timeView.setTextColor(pageItem.color);

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
        contentView.addView(get1TextView(pageContent.title, 32));
        for (String item : pageContent.items){
            contentView.addView(get1TextView(item, 24));
        }
    }

    private View get1TextView(String text, int size){
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(8,8,8,8);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        return textView;
    }


    public View getView(){
        return containerView;
    }
}

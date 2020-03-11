package com.season.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.view.ViewPageView;
import com.season.lib.RoutePath;
import com.season.lib.util.NavigationBarUtil;
import com.season.myapplication.R;


public class MeeActivity extends Activity{

    private ViewPageView mainPageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        NavigationBarUtil.hideNavigationBar(this);

        mainPageView = new ViewPageView(this);
        mainPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainPageView.getCurrentPage() == 0){
                    mainPageView.gotoNextPage();
                }else if (mainPageView.getCurrentPage() == 1){
                    ARouter.getInstance().build(RoutePath.PS).navigation();
                }else{
                    ARouter.getInstance().build(RoutePath.BOOK).navigation();
                }
            }
        });
        mainPageView.addPageView(LayoutInflater.from(this).inflate(R.layout.page_splash, null));
        mainPageView.addPageView(LayoutInflater.from(this).inflate(R.layout.page_ps, null));
        mainPageView.addPageView(LayoutInflater.from(this).inflate(R.layout.page_book, null));

        setContentView(mainPageView);
    }


}

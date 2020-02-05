package com.season.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.view.HAHAView;
import com.season.example.view.LRLRView;
import com.season.lib.RoutePath;
import com.season.lib.dimen.ScreenUtils;
import com.season.myapplication.R;

import java.lang.reflect.Field;

public class MeeActivity extends Activity{

    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mee);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        setDrawerLeftEdgeSize(mDrawerLayout, 0.5f);

        findViewById(R.id.btn_close_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        findViewById(R.id.ll_ps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(RoutePath.PS).navigation();
            }
        });
        findViewById(R.id.ll_book).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(RoutePath.BOOK).navigation();
            }
        });

        hahaView = findViewById(R.id.ps_haha);
        lrlrView = findViewById(R.id.book_lrlr);
    }

    private HAHAView hahaView;
    private LRLRView lrlrView;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hahaView.destroy();
        lrlrView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hahaView.stop();
        //lrlrView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hahaView.start();
        //lrlrView.start();
    }


    /**
     * 抽屉滑动范围控制
     * @param drawerLayout
     * @param displayWidthPercentage 占全屏的份额0~1
     */
    private void setDrawerLeftEdgeSize(DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (drawerLayout == null)
            return;
        try {
            Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (ScreenUtils.getScreenWidth() * displayWidthPercentage)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

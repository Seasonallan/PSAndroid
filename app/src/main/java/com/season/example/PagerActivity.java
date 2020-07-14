package com.season.example;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.season.example.view.PageItem;
import com.season.example.view.PageItemView;
import com.season.example.view.ViewPageView;
import com.season.lib.ui.BaseTLEActivity;
import com.season.lib.ui.PageTurningActivity;
import com.season.myapplication.R;


public class PagerActivity extends BaseTLEActivity {

    @Override
    protected boolean isTopTileEnable() {
        return false;
    }

    private ViewPageView mainPageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //NavigationBarUtil.hideNavigationBar(this);

        mainPageView = new ViewPageView(this);
        mainPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainPageView.isBottomRightClick()){
                    mainPageView.gotoNextPage();
                    return;
                }
                if (v instanceof ViewPageView){
                    View itemView = ((ViewPageView) v).getCurrentView();
                    if (itemView instanceof PageItemView){
                        int position = ((PageItemView) itemView).getPage();
                        String path;
                        if (position == 1) {
                            path = "/ps/main";
                        } else if (position == 2){
                            path = "/plugin/main";
                        } else {
                            path = "/book/shelf/home";
                        }
                        PageTurningActivity.putCacheBitmap(mainPageView.getCurrentPageBitmap());
                        ARouter.getInstance().build(path).navigation();
                    }else{
                        mainPageView.gotoNextPage();
                    }
                }
            }
        });

        mainPageView.addPageView(LayoutInflater.from(PagerActivity.this).inflate(R.layout.page_splash, null));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mainPageView.addPageView(new PageItemView(PagerActivity.this,
                        PageItem.create("图层动画合成", "表情说说 2018")
                                .decorateContent(" • 功能 •", "涂鸦", "图片裁剪", "文字动效", "静动图合成")
                                .decorateContent(" • 核心 •", "时间轴控制", "页面重绘派发")
                                .page(1)
                                .color(COLOR(R.color.global_green))
                ));
                //mainPageView.addPageView(get1ImageView(R.drawable.image_1));
                //mainPageView.addPageView(get1ImageView(R.drawable.image_3));
                mainPageView.addPageView(get1ImageView(R.drawable.image_2));
                mainPageView.addPageView(new PageItemView(PagerActivity.this,
                        PageItem.create("插件动态载入", "插件 2016")
                                .decorateContent(" • 功能 •", "插件", "动态载入",  "APK文件")
                                .decorateContent(" • 核心 •", "反射调用", "静态代理","动态代理", "类加载器", "版本兼容","生命周期")
                                .page(2)
                                .color(COLOR(R.color.global_pink))
                ));
                //mainPageView.addPageView(get1ImageView(R.drawable.image_4));
                mainPageView.addPageView(get1ImageView(R.drawable.image_5));
                mainPageView.addPageView(new PageItemView(PagerActivity.this,
                        PageItem.create("书籍阅读器", "乐阅 2014")
                                .decorateContent(" • 功能 •", "书签", "笔记", "动画", "阅读器")
                                .decorateContent(" • 核心 •", "书籍解析", "页面排版", "动画控制", "事件派发")
                                .page(3)
                                .color(COLOR(R.color.global_yellow))
                ));

                mainPageView.addPageView(get1ImageView(R.drawable.image_6));
                mainPageView.addPageView(get1ImageView(R.drawable.image_7));

            }
        }, 100);

        setContentView(mainPageView);
        mainPageView.scrollShowDelay(1000);


        performCodeWithPermission("App请求访问权限",  new PermissionCallback() {
                    @Override
                    public void hasPermission() {
                    }
                    @Override
                    public void noPermission() {
                    }

                }, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
                Manifest.permission.PROCESS_OUTGOING_CALLS);

    }

    private int COLOR(int id){
        return getResources().getColor(id);
    }

    private ImageView get1ImageView(int id){
        return get1ImageView(id, ImageView.ScaleType.CENTER_CROP);
    }

    private ImageView get1ImageView(int id, ImageView.ScaleType scaleType){
        ImageView imageView = new ImageView(PagerActivity.this);
        imageView.setImageResource(id);
        imageView.setScaleType(scaleType);
        return imageView;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
           // NavigationBarUtil.hideNavigationBar(this);
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainPageView.release();
    }
}

package com.season.example;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.season.mvp.ui.PageTurningActivity;
import com.season.lib.ui.view.LoadingView;
import com.season.lib.BaseContext;
import com.season.lib.support.file.FileUtils;
import com.season.lib.util.LogUtil;
import com.season.lib.util.ToastUtil;
import com.season.lib.ui.view.CircleImageView;
import com.season.plugin.PluginCodeDefine;
import com.season.plugin.PluginHelper;
import com.season.plugin.R;

import java.io.File;


@Route(path= "/plugin/main")
public class PluginActivity extends PageBrokenActivity {

    
    private LoadingView mLoadingView;

    private View containerView;
    private CircleImageView iconView;
    private TextView nameView, packageView, infoView;
    private TextView btnStart, btnInstall;

    private TextView statusView;

    @Override
    protected boolean isTopTileEnable() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //沉浸式状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            //lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            //lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            getWindow().setAttributes(lp);
        }


        containerView = findViewById(R.id.item_container);
        mLoadingView = findViewById(R.id.loadView);

        iconView = findViewById(R.id.item_icon);
        nameView = findViewById(R.id.item_name);
        infoView = findViewById(R.id.item_version);
        packageView = findViewById(R.id.item_package);
        btnStart = findViewById(R.id.item_btn_start);
        btnInstall = findViewById(R.id.item_btn_install);

        statusView = findViewById(R.id.item_status);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!PluginHelper.getInstance().isPluginInstall()){
                    ToastUtil.showToast("服务未启动");
                    return;
                }
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(item.packageInfo.packageName);
                LogUtil.i(item.packageInfo.packageName);
                if (intent == null){
                    intent = new Intent();
                    intent.setPackage(item.packageInfo.packageName);
                    intent.setClassName(item.packageInfo.packageName, "com.season.genglish.ui.SplashActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        btnInstall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!PluginHelper.getInstance().isPluginInstall()){
                    ToastUtil.showToast("服务未启动");
                    return;
                }
                if (PluginHelper.getInstance().isPackageInstall(item.packageInfo.packageName)) {
                    btnInstall.setText("正在卸载中");
                }else{
                    btnInstall.setText("正在安装中");
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (PluginHelper.getInstance().isPackageInstall(item.packageInfo.packageName)){
                            final int resultCode =  PluginHelper.getInstance().deletePackage(item.packageInfo.packageName);
                            BaseContext.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.showToast(PluginCodeDefine.getCodeMessage(resultCode));
                                    resetStartButton();
                                }
                            });
                        }else{
                            final int resultCode =  PluginHelper.getInstance().installApkFile(item.apkFile);
                            BaseContext.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.showToast(PluginCodeDefine.getCodeMessage(resultCode));
                                    resetStartButton();
                                }
                            });
                        }
                    }
                }).start();

            }
        });
        new LoadApkInfoAsyncTask().execute();

        statusView.setText("插件服务正在启动");
        checkServer();
    }

    void checkServer(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PluginHelper.getInstance().isPluginInstall()){
                    statusView.setText("插件服务已启动");
                }else{
                    checkServer();
                }
            }
        }, 500);
    }

    private ApkItem item;
    public class LoadApkInfoAsyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            File apkFile = FileUtils.copyAssets("english.apk");
            PackageInfo info = getPackageManager().getPackageArchiveInfo(apkFile.getPath(), 0);
            item = new ApkItem(PluginActivity.this, info, apkFile.getPath());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            containerView.setVisibility(View.GONE);
            mLoadingView.setLoadingText("复制apk文件");
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mLoadingView.setVisibility(View.GONE);
            containerView.setVisibility(View.VISIBLE);

            iconView.setImageDrawable(item.icon);
            nameView.setText(item.title);
            packageView.setText(item.packageInfo.packageName);
            //infoView.setText(String.format("%s(%s)", item.versionName, item.versionCode));
            infoView.setText(item.apkFile);

            resetStartButton();
        }
    }

    private void resetStartButton(){
        if (PluginHelper.getInstance().isPackageInstall(item.packageInfo.packageName)){
            btnStart.setEnabled(true);
            btnInstall.setText("卸载");
        }else{
            btnInstall.setText("安装");
            btnStart.setEnabled(false);
        }
    }


}

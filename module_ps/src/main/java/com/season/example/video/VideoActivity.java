package com.season.example.video;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.ps.R;
import com.season.example.BigGifActivity;
import com.season.lib.BaseContext;
import com.season.lib.gif.GifMaker;

public class VideoActivity extends Activity {

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    VideoView mVideoView;
    Mp4ToGif mp4ToGif;
    private ProgressDialog progressDialog;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BaseContext.init(getApplicationContext());
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/raw/example"));

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.seekTo(0);
                mVideoView.start();
            }
        });

        mp4ToGif = new Mp4ToGif(getCacheDir() + "/example.gif", new GifMaker.OnGifMakerListener() {
            @Override
            public void onMakeGifStart() {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(VideoActivity.this);
                    //依次设置标题,内容,是否用取消按钮关闭,是否显示进度
                    progressDialog.setTitle("正在合成中");
                    progressDialog.setCancelable(true);
                    //这里是设置进度条的风格,HORIZONTAL是水平进度条,SPINNER是圆形进度条
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setMax(mp4ToGif.getEndTime());
                }
                progressDialog.show();
            }

            @Override
            public void onMakeProgress(int index, int count) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.setTitle("正在合成第 " + index + " 张");
                    progressDialog.setProgress(count);
                }
            }

            @Override
            public void onMakeGifSucceed(String outPath) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                BigGifActivity.start(VideoActivity.this, outPath);
            }

            @Override
            public void onMakeGifFail() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
        findViewById(R.id.to_gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mp4ToGif.videoDecode(VideoActivity.this, Uri.parse("android.resource://" + getPackageName() + "/raw/example"),
                                mVideoView.getDuration());
                    }
                }).start();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

package com.season.example.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.ps.R;
import com.season.example.BigGifActivity;
import com.season.example.CropActivity;
import com.season.lib.BaseContext;
import com.season.lib.gif.GifMaker;
import com.season.lib.util.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

public class VideoActivity extends Activity {

    public static void start(Context context){
        Intent intent = new Intent(context, VideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
    VideoView mVideoView;
    Mp4ToGif mp4ToGif;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BaseContext.init(getApplicationContext());
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/raw/example"));

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.seekTo(0);
                mVideoView.start();
            }
        });
        findViewById(R.id.to_gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        try {
                            mp4ToGif = new Mp4ToGif(getCacheDir() +"/example.gif");
                            mp4ToGif.videoDecode(VideoActivity.this, Uri.parse("android.resource://"+getPackageName()+"/raw/example"),
                                    0, mVideoView.getDuration());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    BigGifActivity.start(VideoActivity.this, mp4ToGif.filePath);
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

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

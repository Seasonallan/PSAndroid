package com.season.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import com.example.ps.R;
import com.season.lib.BaseContext;
import com.season.lib.file.FileManager;
import com.season.lib.http.DownloadAPI;
import com.season.lib.util.ToastUtil;
import com.season.lib.view.gif.GifFrameView;

import java.io.File;

public class BigGifActivity extends Activity {

    public static void start(Context context, String urlPath){
        Intent intent = new Intent(context, BigGifActivity.class);
        intent.putExtra("path", urlPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    String urlPath;
    LinearLayout linearLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BaseContext.init(getApplicationContext());

        urlPath = getIntent().getStringExtra("path");

        linearLayout = findViewById(R.id.gif_cot);


        final File file = FileManager.getPsFile(urlPath.hashCode() + "", "gif");
        if (file == null) {
            return;
        }
        DownloadAPI.downloadFile(urlPath, file, new DownloadAPI.IDownloadListener() {
            @Override
            public void onCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (file.length() > 0) {
                            for (int i=0;i< 40;i++){
                                GifFrameView gifView = new GifFrameView(BigGifActivity.this);
                                gifView.setMovieResource(file.toString());
                                gifView.setPosition(i);
                                linearLayout.addView(gifView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            }
                        }
                    }
                });
            }

            @Override
            public void onError() {
                file.deleteOnExit();
                ToastUtil.show("下载失败，请稍候重试");
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (linearLayout != null && linearLayout.getChildCount() > 0){
            for (int i = 0; i< linearLayout.getChildCount(); i++){
                View view = linearLayout.getChildAt(i);
                if (view instanceof GifFrameView){
                    ((GifFrameView) view).onRelease();
                }
            }
        }
    }

}

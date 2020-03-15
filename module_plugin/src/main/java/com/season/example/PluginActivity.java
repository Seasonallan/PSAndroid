package com.season.example;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.season.lib.BaseContext;
import com.season.lib.RoutePath;
import com.season.plugin.R;
import com.season.example.adapter.ApkItem;
import com.season.example.adapter.MainAdapter;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Route(path= RoutePath.PLUGIN)
public class PluginActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.id_listview);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);

        mSwipeLayout.setOnRefreshListener(this);

        new LoadApkInfoAsyncTask().execute();
    }

    public class LoadApkInfoAsyncTask extends AsyncTask<Void, Void, List<ApkItem>>{

        @Override
        protected List<ApkItem> doInBackground(Void... params) {
            List<ApkItem> result = new ArrayList<>();

            //assets目录apk文件测试
            String[] assetsNames = {"english.apk"};
            PackageManager pm = getPackageManager();
            for (String assetsName : assetsNames) {
                copyAssets(assetsName);
                File apkFile = getFileStreamPath(assetsName);
                PackageInfo info = pm.getPackageArchiveInfo(apkFile.getPath(), 0);
                ApkItem apkItem = new ApkItem(PluginActivity.this, info, apkFile.getPath());
                result.add(apkItem);
            }
            //sdcard目录apk文件测试
//            String[] assetsNames = {"KuaifangTVOnline.apk","plugindemo-debug.apk","app-debug.apk"};
//            PackageManager pm = getPackageManager();
//            for (String assetsName : assetsNames) {
//                File apkFile = new File(android.os.Environment.getExternalStorageDirectory() ,assetsName);
//                PackageInfo info = pm.getPackageArchiveInfo(apkFile.getPath(), 0);
//                ApkItem apkItem = new ApkItem(PluginActivity.this, info, apkFile.getPath());
//                result.add(apkItem);
//            }
            return result;
        }

        @Override
        protected void onPostExecute(List<ApkItem> apkItems) {
            super.onPostExecute(apkItems);
            mSwipeLayout.setRefreshing(false);
            mAdapter = new MainAdapter(PluginActivity.this, apkItems);
            mListView.setAdapter(mAdapter);
        }
    }


    /**
     * 把Assets里面得文件复制到 /data/data/files 目录下
     *
     * @param sourceName
     */
    public static void copyAssets(String sourceName) {
        AssetManager am = BaseContext.getInstance().getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = am.open(sourceName);
            File extractFile = BaseContext.getInstance().getFileStreamPath(sourceName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }

    }
    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
            // ignore
        }
    }

    @Override
    public void onRefresh() {
        new LoadApkInfoAsyncTask().execute();
    }
}

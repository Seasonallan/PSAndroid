package com.example.lib.http;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 */
public class DownloadAPI {


    public static void downloadFile(final String httpUrl, final File file, final IDownloadListener iDownloadListener) {
        new Thread() {

            @Override

            public void run() {

                URL url;

                HttpURLConnection connection;

                try {
                    url = new URL(httpUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    FileOutputStream outputStream = new FileOutputStream(file);
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //得到服务器响应的输入流
                        InputStream inputStream = connection.getInputStream();
                        //获取请求的内容总长度
                        int contentLength = connection.getContentLength();
                        BufferedInputStream bfi = new BufferedInputStream(inputStream);
                        //此处的len表示每次循环读取的内容长度
                        int len;
                        //已经读取的总长度
                        int totle = 0;
                        //bytes是用于存储每次读取出来的内容
                        byte[] bytes = new byte[1024];
                        while ((len = bfi.read(bytes)) != -1) {
                            //每次读取完了都将len累加在totle里
                            totle += len;
                            //每次读取的都更新一次progressBar
                       //     mPb.setProgress(totle);
                            //通过文件输出流写入从服务器中读取的数据
                            outputStream.write(bytes, 0, len);
                        }
                        //关闭打开的流对象
                        outputStream.close();
                        inputStream.close();
                        bfi.close();
                        iDownloadListener.onCompleted();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                iDownloadListener.onError();
            }

        }.start();
    }

    public interface IDownloadListener{
        void onCompleted();
        void onError();
    }

}

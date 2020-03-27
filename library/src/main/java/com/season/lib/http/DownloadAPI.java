package com.season.lib.http;

import androidx.annotation.NonNull;

import com.season.lib.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 网络请求
 */
public class DownloadAPI {


    /**
     * 下载文件
     *
     * @param httpUrl
     * @param file
     * @param iDownloadListener
     */
    public static void downloadFile(@NonNull final String httpUrl, @NonNull final File file, @NonNull final IDownloadListener iDownloadListener) {
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
                        if (iDownloadListener != null)
                            iDownloadListener.onCompleted();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (iDownloadListener != null)
                    iDownloadListener.onError();
            }

        }.start();
    }



    /**
     * 下载文件， 非线程中
     *
     * @param httpUrl
     * @param file
     */
    public static void downloadFile(@NonNull final String httpUrl, @NonNull final File file) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * GET方式请求数据
     *
     * @param urlStr
     */
    public static void getRequestThread(@NonNull final String urlStr, @NonNull final IHttpRequestListener listener) {
        new Thread() {
            @Override
            public void run() {
                try {
                    listener.onCompleted(getRequest(urlStr));
                } catch (Exception e) {
                    listener.onError();
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * GET方式请求数据
     *
     * @param urlStr
     */
    public static String getRequest(@NonNull final String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);
        httpURLConnection.connect();
        if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
            //调用getInputStream后才开始正式发起请求
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            //关闭流对像
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    /**
     * Post方式请求数据
     *
     * @param urlStr
     * @param params
     */
    public static void postRequestThread(@NonNull final String urlStr, @NonNull final Map<String, String> params, @NonNull final IHttpRequestListener listener) {
        new Thread() {
            @Override
            public void run() {
                try {
                    listener.onCompleted(postRequest(urlStr, params));
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onError();
                }
            }

        }.start();
    }

    public static String postRequest(@NonNull final String urlStr, @NonNull final Map<String, String> params) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);
        //设置请求方式，默认为GET
        httpURLConnection.setRequestMethod("POST");
        //设置允许向httpURLConnection中写入数据，写入的数据都放在内存缓冲区中
        //如果是GET请求，不能设置为true，否则会获取不到数据
        httpURLConnection.setDoOutput(true);
        //获取输出流向里面写入数据一定要在建立连接之前，否则会抛出异常
        OutputStream outputStream = httpURLConnection.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        String paramsString = getPostParams(params);
        LogUtil.i(urlStr);
        LogUtil.i(paramsString);
        printWriter.write(paramsString);
        printWriter.close();
        outputStream.close();
        httpURLConnection.connect();
        if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
            //开始发起请求
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            //依次关闭流对象
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    /**
     * 将Map集合中的数据转换成post提交的格式
     *
     * @param params
     * @return
     */
    private static String getPostParams(Map<String, String> params) {
        String paramsString = null;
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                if (paramsString == null) {
                    paramsString = key + "=" + value;
                } else {
                    paramsString += "&" + key + "=" + value;
                }
            }
        }
        return paramsString;
    }

    /**
     * 文件下载回调
     */
    public interface IDownloadListener {
        void onCompleted();

        void onError();
    }

    /**
     * 网络请求回调
     */
    public interface IHttpRequestListener {
        void onCompleted(String result);

        void onError();
    }

}

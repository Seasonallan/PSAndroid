package com.ripple;

import androidx.annotation.NonNull;

import com.ripple.LogRipple;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class SimpleRequest {

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


    public static String postRequest(@NonNull final String urlStr, @NonNull final String paramsString) {
        try {
            LogRipple.e("Net", urlStr);
            LogRipple.e("Net", paramsString);
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            //设置请求方式，默认为GET
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", " application/json");
            //设置允许向httpURLConnection中写入数据，写入的数据都放在内存缓冲区中
            //如果是GET请求，不能设置为true，否则会获取不到数据
            httpURLConnection.setDoOutput(true);
            //获取输出流向里面写入数据一定要在建立连接之前，否则会抛出异常
            OutputStream outputStream = httpURLConnection.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
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
                String response = stringBuilder.toString();
                LogRipple.e("Net", response);
                return response;
            }
        } catch (Exception e) {

            LogRipple.error("exception", e);
        }
        return null;
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
            for (Map.Entry<String, String> next : entrySet) {
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

}

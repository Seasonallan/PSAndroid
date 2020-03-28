package com.season.example.transfer.serv;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

import com.season.example.transfer.serv.req.HttpDeleteHandler;
import com.season.example.transfer.serv.req.HttpDownloadHandler;
import com.season.example.transfer.serv.req.HttpGetFilesHandler;
import com.season.example.transfer.serv.req.HttpIndexHandler;
import com.season.example.transfer.serv.req.HttpProgressHandler;
import com.season.example.transfer.serv.req.HttpUploadHandler;
import com.season.example.transfer.util.CommonUtil;
import com.season.example.transfer.util.Constants;

/**
 * @brief Web服务类
 * @author join
 */
public class WebServerThread extends Thread { 

    public static final int ERR_UNEXPECT = 0x0101;
    public static final int ERR_PORT_IN_USE = 0x0102;
    public static final int ERR_TEMP_NOT_FOUND = 0x0103; 

    private Context mContext;
    private ServerSocket serverSocket;
    static boolean isLoop;

    private OnWebServListener mListener; 
    private ExecutorService pool; // 线程池

    public WebServerThread(Context context) {
        super(); 
        isLoop = false;
        mContext = context;
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            while (CommonUtil.isLocalPortInUse(Constants.PORT)) { 
            	Constants.PORT --;
            }
            // 创建服务器套接字
            serverSocket = new ServerSocket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(Constants.PORT);
            serverSocket.bind(inetSocketAddress);

            // 设置端口重用
            serverSocket.setReuseAddress(true);
            // 创建HTTP协议处理器
            BasicHttpProcessor httpproc = new BasicHttpProcessor();
            // 增加HTTP协议拦截器
            httpproc.addInterceptor(new ResponseDate());
            httpproc.addInterceptor(new ResponseServer());
            httpproc.addInterceptor(new ResponseContent());
            httpproc.addInterceptor(new ResponseConnControl());
            // 创建HTTP服务
            HttpService httpService = new HttpService(httpproc,
                    new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
            // 创建HTTP参数
            HttpParams params = new BasicHttpParams();
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WebServer/1.1");
            // 设置HTTP参数
            httpService.setParams(params);
            // 创建HTTP请求执行器注册表
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            // 增加HTTP请求执行器  
            reqistry.register(UrlPattern.DELETE, new HttpDeleteHandler(mContext, mListener));
            reqistry.register(UrlPattern.UPLOAD, new HttpUploadHandler(mContext, mListener));
            reqistry.register(UrlPattern.PROGRESS, new HttpProgressHandler(mListener));
            reqistry.register(UrlPattern.DOWNLOAD, new HttpDownloadHandler());
            reqistry.register(UrlPattern.BROWSE, new HttpIndexHandler(mContext, mListener));
            reqistry.register(UrlPattern.GET_FILES, new HttpGetFilesHandler(mContext, mListener));
            // 设置HTTP请求执行器
            httpService.setHandlerResolver(reqistry);
            // 回调通知服务开始
            if (mListener != null) {
                mListener.onStarted("http://"+CommonUtil.getLocalIpAddress(mContext)+":" +Constants.PORT);
            }
            /* 循环接收各客户端 */
            isLoop = true;
            while (isLoop && !Thread.interrupted()) {
                // 接收客户端套接字
                Socket socket = serverSocket.accept();
                // 绑定至服务器端HTTP连接
                conn = new DefaultHttpServerConnection();
                conn.bind(socket, params);
                // 派送至WorkerThread处理请求
                Thread t = new WorkerThread(httpService, conn, mListener);
                t.setDaemon(true); // 设为守护线程
                pool.execute(t); // 执行
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (isLoop) { // 以排除close造成的异常
                // 回调通知服务出错
                if (mListener != null) {
                    mListener.onError(ERR_UNEXPECT);
                } 
                isLoop = false;
            }
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                // 回调通知服务结束
                if (mListener != null) {
                    mListener.onStopped();
                }
            } catch (IOException e) {
            }
        }
    }DefaultHttpServerConnection conn;

    public void close() {
        isLoop = false; 
        try {
            if (conn != null) {
            	conn.shutdown();
            	conn.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
        }
    }

    public interface OnWebServListener {
        void onStarted(String ip);

        void onStopped();

        void onError(int code);
        
        void onPercent(String fileName, int percent);

        void onWebFileAdded(String fileName);
        
        void onWebFileUploadError(String fileName, String error);
        
        void onLocalFileDeleted(String fileName);
        
        void onComputerConnect();
    }

    public void setOnWebServListener(OnWebServListener mListener) {
        this.mListener = mListener;
    }

}

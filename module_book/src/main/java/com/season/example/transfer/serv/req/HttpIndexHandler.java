package com.season.example.transfer.serv.req;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;

import com.season.example.transfer.serv.WebServerThread.OnWebServListener;
import com.season.example.transfer.serv.support.Progress;
import com.season.example.transfer.util.Constants;

/**
 * @brief 目录浏览页面请求处理
 * @author join
 */
public class HttpIndexHandler implements HttpRequestHandler {
   
	private Context mContext;
	private OnWebServListener mListener;

	public HttpIndexHandler(Context context, OnWebServListener mListener) { 
		this.mContext = context;
		this.mListener = mListener;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException { 
		String requestUrl = URLDecoder.decode(request.getRequestLine().getUri(),
				Constants.ENCODING);
    	if(requestUrl.indexOf(".") == -1){
			response.setHeader("Content-Type", "text/html;charset=" + Constants.ENCODING);
    		requestUrl = "/temp/index.htm";
    	}
    	requestUrl = "wifi"+requestUrl; 
		HttpEntity entity;  
		InputStream stream = mContext.getAssets().open(requestUrl);
		entity = new InputStreamEntity(stream, -1); 
		if(mListener != null){
			mListener.onComputerConnect();
		}
       if(requestUrl.endsWith("css")){
    	   response.setHeader("Content-Type", "text/css"); 
       }
		response.setEntity(entity);
		Progress.clear();
	} 
	  
 
}

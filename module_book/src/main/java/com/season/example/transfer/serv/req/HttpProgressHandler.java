package com.season.example.transfer.serv.req;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONException;
import org.json.JSONObject;

import com.season.example.transfer.serv.WebServerThread.OnWebServListener;
import com.season.example.transfer.serv.support.HttpGetParser;
import com.season.example.transfer.serv.support.Progress;
import com.season.example.transfer.util.Constants;
import com.season.lib.support.file.FileUtils;

public class HttpProgressHandler implements HttpRequestHandler { 

    private OnWebServListener mListener;
    public HttpProgressHandler(OnWebServListener mListener) {
    	this.mListener = mListener;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {  
        HttpGetParser parser = new HttpGetParser();
        Map<String, String> params = parser.parse(request);
        String id = params.get("fname");
        if (id == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        id = URLDecoder.decode(id, Constants.ENCODING);
        int progress = Progress.getProgress(id); 
        if(mListener != null){
        	mListener.onPercent(id, progress);
        }
        progress = progress<=0?0:progress;  
        JSONObject object = new JSONObject();
        try {
			object.put("fileName", id);
			object.put("progress", progress/100f);
			object.put("size", FileUtils.readableFileSize(Progress.getSize(id)));
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		String progressJson = object.toString();   
        response.setEntity(new StringEntity(progressJson, Constants.ENCODING));
    }

}

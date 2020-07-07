package com.season.example.transfer.serv.req;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.season.example.transfer.serv.WebServerThread.OnWebServListener;
import com.season.example.transfer.util.Constants;
import com.season.example.transfer.util.SaverUtil;
import com.season.lib.support.file.FileUtils;

public class HttpGetFilesHandler implements HttpRequestHandler { 
 
    private Context mContext;
    public HttpGetFilesHandler(Context context, OnWebServListener mListener) { 
    	this.mContext = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException { 
    	JSONObject obj = new JSONObject();
		JSONArray fileRows = new JSONArray();
		List<String> fileLists = SaverUtil.getSingleton(mContext)
				.getHistoryFileLists(); 
		for (String string : fileLists) { 
			try {
				fileRows.put(buildFileRow(string));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			obj.put("userName", android.os.Build.MODEL);
			obj.put("fileList", fileRows);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String fileRowsd = obj.toString(); 
        response.setEntity(new StringEntity(fileRowsd, Constants.ENCODING));
    }

	private JSONObject buildFileRow(String f) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("name", f); 
		obj.put("size", intelligenceGetFileSize(f)); 
		return obj;
	}
	
	String intelligenceGetFileSize(String fileName){
		File file = new File(Constants.UPLOAD_DIR + fileName);
		if(file.exists()){
			return ""+ FileUtils.readableFileSize(file.length());
		}else{
			return "文件不存在";
		}
	}
}



package com.season.example.transfer.serv.req;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;

import com.season.example.transfer.serv.WebServerThread.OnWebServListener;
import com.season.example.transfer.serv.support.HttpPostParser;
import com.season.example.transfer.util.Constants;
import com.season.example.transfer.util.SaverUtil;

/**
 * @brief 删除请求处理
 * @author join
 */
public class HttpDeleteHandler implements HttpRequestHandler {

    private Context mContext;
    private OnWebServListener mListener;

    public HttpDeleteHandler(Context context, OnWebServListener listener) {
        this.mContext = context;
        this.mListener= listener;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {  
        if (!HttpPostParser.isPostMethod(request)) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            return;
        }
        HttpPostParser parser = new HttpPostParser();
        Map<String, String> params = parser.parse(request);
        String fname = params.get("fname");
        Header referer = request.getFirstHeader("Referer");
        if (fname == null || referer == null) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        fname = URLDecoder.decode(fname, Constants.ENCODING); 
        final File file; 
        file = new File(Constants.UPLOAD_DIR, fname); 

        file.delete(); 
        response.setStatusCode(HttpStatus.SC_OK);
        if(!file.exists()){
        	if(mListener != null && SaverUtil.getSingleton(mContext).deleteFileFromList(fname)){
        		mListener.onLocalFileDeleted(fname);
        	} 
        }
        StringEntity entity = new StringEntity(file.exists() ? "1" : "0", Constants.ENCODING); // 1: 失败；0：成功。
        response.setEntity(entity);
    }
  
}

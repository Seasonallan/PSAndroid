package com.season.example.transfer.serv.req;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.season.example.transfer.fileupload.FileItem;
import com.season.example.transfer.fileupload.FileItemFactory;
import com.season.example.transfer.fileupload.FileUploadException;
import com.season.example.transfer.fileupload.ProgressListener;
import com.season.example.transfer.fileupload.disk.DiskFileItemFactory;
import com.season.example.transfer.fileupload.httpserv.HttpServFileUpload;
import com.season.example.transfer.fileupload.httpserv.HttpServRequestContext;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.text.TextUtils;

import com.season.example.transfer.serv.WebServerThread.OnWebServListener;
import com.season.example.transfer.serv.support.Progress;
import com.season.example.transfer.util.Constants;
import com.season.example.transfer.util.SaverUtil;

/**
 * @brief 上传请求处理
 * @waring 1) `Unsafe JavaScript attempt to access frame...` maybe occur in
 *         chrome, which caused by iframe way of `ajaxfileupload.js`. more:
 *         `http
 *         ://stackoverflow.com/questions/5660116/unsafe-javascript-attempt-
 *         to-access-frame-in-google-chrome`
 * @author join
 */
public class HttpUploadHandler implements HttpRequestHandler { 
	private Context mContext;
	private OnWebServListener mListener;

	public HttpUploadHandler(Context context, OnWebServListener listener) {
		this.mContext = context;
		this.mListener = listener;
	} 
	
	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException { 
		if (!HttpServFileUpload.isMultipartContent(request)) {
			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			return;
		}
		File uploadDir = new File(Constants.UPLOAD_DIR);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}
		if (uploadDir.isDirectory()) { 
			boolean res = processFileUpload(request, uploadDir);
			response.setStatusCode(res?HttpStatus.SC_OK:HttpStatus.SC_BAD_REQUEST);  
			response.setEntity(new StringEntity("ok", Constants.ENCODING)); 
		} else {
			response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
		}
	}
  
	/** Create a progress listener */
	class MyProgressListener implements ProgressListener {
		private String mFileName;
		private long mFileSize;
		@Override
		public void update(long pBytesRead, long pContentLength, int pItems) { 
			if (pContentLength != -1 && !TextUtils.isEmpty(mFileName)) {
				int progress = (int) (pBytesRead * 100 / pContentLength);
				Progress.update(mFileName, mFileSize, progress); 
		        if(mListener != null){
		        	mListener.onPercent(mFileName, progress);
		        } 
			}
		}
		@Override
		public void setPrimaryKey(String key) {
			mFileName = key;
		}
		@Override
		public void setTotalSize(long size) { 
			mFileSize = size;
		} 

	}
	/** Process file upload 
	 * @throws Exception */
	private boolean processFileUpload(HttpRequest request, File uploadDir) {
		boolean res = true;  
		FileItemFactory factory = new DiskFileItemFactory(
				Constants.THRESHOLD_UPLOAD, uploadDir);
		HttpServFileUpload fileUpload = new HttpServFileUpload(factory); 
		fileUpload.setProgressListener(new MyProgressListener());
		List<FileItem> fileItems = new ArrayList<FileItem>();
		try {
			fileItems = fileUpload
					.parseRequest(new HttpServRequestContext(request), mListener);
		} catch (FileUploadException e) { 
			e.printStackTrace();
			res = false; 
		}    
		for (FileItem item : fileItems) {
			if (!item.isFormField()) { 
				String fileName = processUploadedFile(item, uploadDir);
				if(mListener != null && SaverUtil.getSingleton(mContext).addFile2List(fileName)){
					mListener.onWebFileAdded(fileName);
				} 
			}
		} 
		return res;
	} 
	  
	/** Process a file upload [rename the tmp file]*/
	private String processUploadedFile(FileItem item, File uploadDir) {
		String fileName = item.getName();
		File uploadedFile = new File(uploadDir, fileName);
		try {
			item.write(uploadedFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}


}

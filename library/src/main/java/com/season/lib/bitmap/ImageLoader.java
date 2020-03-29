package com.season.lib.bitmap;

import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import com.season.lib.BaseContext;
import com.season.lib.http.DownloadAPI;

/**
 *
 * 简易图片下载器
 *
 */
public class ImageLoader extends BaseImageLoader { 
	
	public static class LoadParam{
		public DisplayMetrics display;
		public DisplayMetrics getDisplayMetrics(){
			return display;
		}
		LoadParam(){
			this.display = BaseContext.getDisplayMetrics();
		}
	}


	private HashMap<ImageView, String> mLoadingTag;
	private ImageMemoryCache mImageMemoryCache;
	private LoadParam mLoadParam;
	public static ImageLoader getInstance(){
		if (!sImageLoader.containsKey("Default")) {
			sImageLoader.put("Default", new ImageLoader(new LoadParam()));
		}
		return sImageLoader.get("Default");
	}

	static HashMap<String, ImageLoader> sImageLoader = new HashMap<String, ImageLoader>();
	public static ImageLoader getInstance(int maxWidth, int maxHeight){
		if (!sImageLoader.containsKey(maxWidth+"-"+maxHeight)) {
			LoadParam param = new LoadParam();
			DisplayMetrics display = new DisplayMetrics();
			display.widthPixels = maxWidth;
			display.heightPixels = maxHeight;
			param.display = display;
			sImageLoader.put(maxWidth+"-"+maxHeight, new ImageLoader(param));
		}
		return sImageLoader.get(maxWidth+"-"+maxHeight);
	}

	public ImageLoader(LoadParam param) {
		this.mLoadParam = param;
		this.mLoadingTag = new HashMap<>();
		this.mImageMemoryCache = ImageMemoryCache.getInstance();
	}

	public void setImageViewBitmap(String imageUrl, String filePath,
			final ImageView imageView, final int defaultResId) {
		if (TextUtils.isEmpty(imageUrl) && TextUtils.isEmpty(filePath)) {
			imageView.setImageResource(defaultResId);
			return;
		}
		if (mLoadingTag.containsKey(imageView)) {
			return;
		}
		String imageId = getImageId(imageUrl, filePath);
		mLoadingTag.put(imageView, imageId);
		imageView.setTag(imageId);
		Bitmap bitmap = loadImage(imageUrl, filePath, new ImageCallback() {
			@Override
			public void imageLoaded(Bitmap bitmap, String url, String path) {
				View view = null;
				if(getImageId(url, path).equals(imageView.getTag())){
					view = imageView;
				}
				mLoadingTag.remove(view);
				if (view != null) {
					setImageViewBitmap((ImageView) view, bitmap, defaultResId);
				} 
			}
		});
		setImageViewBitmap(imageView, bitmap, defaultResId);
	}
	
	private void setImageViewBitmap(ImageView view, Bitmap bitmap,
			int defaultResId) {
		if (bitmap != null) {
			view.setImageBitmap(bitmap);
		} else {
			view.setImageResource(defaultResId);
		}
	}

	@Override
	protected boolean isInCache(String imageId) {
		boolean result = mImageMemoryCache.isContains(imageId);
		return result;
	}

	@Override
	protected Bitmap loadImageFromSdcard(String imageUrl, String filePath) {
		if (TextUtils.isEmpty(filePath)){
			filePath = getCacheFile(imageUrl.hashCode()+"");
		}
		File file = new File(filePath);
		if (file.isFile() && file.length() > 0){
			Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			Bitmap scaleBitmap = BitmapUtil.scale(bitmap, mLoadParam.display.widthPixels, mLoadParam.display.heightPixels);
			BitmapUtil.recycleBitmaps(bitmap);
			return scaleBitmap;
		}
		return null;
	}

	@Override
	protected Bitmap loadImageFromNetwork(String imageUrl, String filePath) {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		Bitmap bitmap = null;
		URI uri = null;
		
		if (TextUtils.isEmpty(imageUrl)) {
			return bitmap;
		}
		try{
			uri = URI.create(imageUrl);
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(uri == null){
			return bitmap;
		}
		if(bitmap == null && imageUrl != null){
			String file = getCacheFile(imageUrl.hashCode()+"");
			DownloadAPI.downloadFile(imageUrl, new File(file));
            bitmap = loadImageFromSdcard(imageUrl, file);
		}
		
		return bitmap;
	}

	private String getCacheFile(String fend){
		String pathDir = BaseContext.getInstance().getCacheDir() + File.separator;
		String path =pathDir + "image-"+fend;
		File fileDir = new File(pathDir);
		if(!fileDir.exists()){
			fileDir.mkdirs();
		}
		return path;
	}

	@Override
	protected SoftReference<Bitmap> loadImageInCache(String imageId) {
		Bitmap bitmap = mImageMemoryCache.get(imageId);
		if (bitmap != null) {
			return new SoftReference(bitmap);
		}
		return null;
	}

	@Override
	protected void saveImageToCache(Bitmap bitmap, String imageId) {
		if (bitmap == null || TextUtils.isEmpty(imageId)) {
			return;
		}
		mImageMemoryCache.put(imageId, bitmap);
	} 
	

	@Override
	protected void remove(String imageId){
		mImageMemoryCache.remove(imageId);
	}
	

}

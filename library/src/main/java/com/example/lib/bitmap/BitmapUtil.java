package com.example.lib.bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.example.lib.BaseContext;


public class BitmapUtil {

	/**
	 * 回收图片
	 * @param bitmaps
	 */
	public static void recycleBitmaps(Bitmap... bitmaps) {
		for (Bitmap bitmap : bitmaps) {
			try {
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			bitmaps = null;
		}
	}


	/**
	 * 保存图片
	 * @param output
	 * @param bitmap
	 * @return
	 */
	public static String saveBitmap(File output, Bitmap bitmap) {
		try {
			FileOutputStream fos = new FileOutputStream(output);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
			return output.getPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 裁剪图片
	 * @param bitmap
	 * @param width
	 * @param height
	 * @param x
	 * @param y
	 * @return
	 */
	public static Bitmap cutBitmap(Bitmap bitmap, float width, float height, float x, float y)
	{
		return cutBitmap(bitmap, width, height, x, y, true);
	}
	public static Bitmap cutBitmap(Bitmap bitmap, float width, float height, float x, float y, boolean recycle)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (x > w){
			x = 0;
		}
		if (y > h){
			y = 0;
		}
		if (width + x > w){
			width = w - x;
		}
		if (height + y > h){
			height = h - y;
		}
		Bitmap bitmapResult = Bitmap.createBitmap(bitmap, (int)x, (int)y, (int)width, (int)height);
		if (recycle)
			bitmap.recycle();
		return bitmapResult;
	}

    /**
     * 获取中间正方形的bitmap
     * @param bitmap
     * @return
     */
	public static Bitmap centerCropBitmap(Bitmap bitmap)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (w == h){
			return bitmap;
		}
		int padding = Math.abs(w - h)/2;
		Bitmap bitmapResult;
		if (w > h){
			bitmapResult = Bitmap.createBitmap(bitmap, padding, 0, h, h);
		}else{
			bitmapResult = Bitmap.createBitmap(bitmap, 0, padding, w, w);
		}
		bitmap.recycle();
		return bitmapResult;
	}


	/**
	 * 缩放bitmap
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap scale(Bitmap bitmap, float width, float height)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scale = width / w;
		float scale2 = height / h;
		scale = scale < scale2 ? scale : scale2;
		matrix.postScale(scale, scale);
		Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return bmp;
	}
	/**
	 * 缩放bitmap
	 * @param bitmap
	 * @param destinationHeight
	 * @return
	 */
    public static Bitmap scale(Bitmap bitmap, float destinationHeight) {
        Matrix matrix = new Matrix();
        matrix.postScale(destinationHeight/bitmap.getHeight(), destinationHeight/bitmap.getHeight()); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }

	/**
	 * 根据屏幕宽高缩放图片
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static Bitmap clipScreenBoundsBitmap(InputStream is) throws IOException {
		return clipScreenBoundsBitmap(is, 1);
	}

	public static Bitmap clipScreenBoundsBitmap(InputStream is, float scaled) throws IOException {
		if (is != null) {
			if (!is.markSupported()) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				is.close();
				is = new ByteArrayInputStream(baos.toByteArray(), 0, baos.size());
			}
			DisplayMetrics display = BaseContext.getDisplayMetrics();
			Options opts = new Options();
			opts.inScaled = false;
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, opts);
			int targetW = (int) (display.widthPixels * scaled);
			int targetH = (int) (display.heightPixels * scaled);
			int imgW = opts.outWidth;
			int imgH = opts.outHeight;
			is.reset();
			Bitmap bitmap = clipBitmap(is, targetW, targetH, imgW,
					imgH);
			is.close();
			return bitmap;
		}
		return null;
	}

	public static Bitmap clipBitmap(InputStream is,
			int targetW, int targetH, int imgW, int imgH) {
		if (is != null) {
			DisplayMetrics display = BaseContext.getDisplayMetrics();
			Options opts = new Options();
			float densityScaled = 1;
			float imgSize = imgW * imgH;
			float targetSize = targetW * targetH;
			if (imgSize > targetSize) {
				densityScaled = targetSize / imgSize;
			}
			opts = new Options();
			opts.inTargetDensity = display.densityDpi;
			opts.inDensity = (int) (display.densityDpi / densityScaled);
			opts.inPreferredConfig = Config.RGB_565;
			opts.inScaled = true;
			return BitmapFactory.decodeStream(is, null, opts);
		}
		return null;
	}


	/**
	 * drawable转化为bitmap
	 * @param drawable
	 * @return
	 */
	public static Bitmap getBitmapFromDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		try {
			Bitmap bitmap;
			if (drawable instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			}
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

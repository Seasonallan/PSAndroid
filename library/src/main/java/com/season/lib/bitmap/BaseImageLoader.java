package com.season.lib.bitmap;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.season.lib.BaseContext;
import com.season.lib.os.MulThreadPool;


public abstract class BaseImageLoader {

    /**
     * 线程池
     */
    private MulThreadPool mThreadPool;

    public Bitmap loadImage(final String imageUrl, final String filePath,
                            final ImageCallback callback) {
        if (mThreadPool == null) {
            mThreadPool = new MulThreadPool();
        }
        final String imageId = getImageId(imageUrl, filePath);
        if (isInCache(imageId)) {
            SoftReference<Bitmap> softReference = loadImageInCache(imageId);
            if (softReference != null && softReference.get() != null) {
                return softReference.get();
            }
        }
        mThreadPool.addTask(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = loadImageFromSdcard(imageUrl, filePath);
                if (bitmap != null) {
                    saveImageToCache(bitmap, imageId);
                    BaseContext.getHandler().post(new Runnable() {

                        @Override
                        public void run() {
                            callback.imageLoaded(bitmap, imageUrl, filePath);
                        }
                    });
                } else {
                    final Bitmap bitmapNet = loadImageFromNetwork(imageUrl, filePath);
                    if (bitmapNet != null) {
                        saveImageToCache(bitmapNet, imageId);
                        BaseContext.getHandler().post(new Runnable() {

                            @Override
                            public void run() {
                                callback.imageLoaded(bitmapNet, imageUrl, filePath);
                            }
                        });
                    } else {
                        BaseContext.getHandler().post(new Runnable() {

                            @Override
                            public void run() {
                                callback.imageLoaded(bitmap, imageUrl, filePath);
                            }
                        });
                    }
                }
            }
        });
        return null;
    }

    protected String getImageId(String url, String path) {
        if (!TextUtils.isEmpty(path)) {
            return path.hashCode() + "";
        }
        if (!TextUtils.isEmpty(url)) {
            return url.hashCode() + "";
        }
        return "";
    }

    protected abstract void remove(String imageId);

    protected abstract boolean isInCache(String imageId);

    protected abstract SoftReference<Bitmap> loadImageInCache(String imageId);

    protected abstract void saveImageToCache(Bitmap bitmap, String imageId);

    protected abstract Bitmap loadImageFromSdcard(String imageUrl,
                                                  String filePath);

    protected abstract Bitmap loadImageFromNetwork(String imageUrl,
                                                   String filePath);

    public interface ImageCallback {
        void imageLoaded(Bitmap bitmap, String imageUrl, String filePath);
    }

}
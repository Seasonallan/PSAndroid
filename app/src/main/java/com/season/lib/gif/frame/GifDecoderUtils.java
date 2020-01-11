package com.season.lib.gif.frame;

import android.graphics.Bitmap;
import android.graphics.Movie;
import android.text.TextUtils;

import com.season.lib.gif.movie.DelayDecoder;
import com.season.lib.gif.movie.FrameDecoder;
import com.season.lib.gif.utils.Util;
import com.season.myapplication.BuildConfig;
import com.season.lib.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lizhongxin on 15/09/2018.
 */

public class GifDecoderUtils {
    private volatile static GifDecoderUtils singleton;
    private Movie mMovie;
    private GifDecoder gifDecoder = null;
    private Bitmap firstFrame;
    private String path;

    public static GifDecoderUtils getSingleton() {
        if (singleton == null) {
            synchronized (GifDecoderUtils.class) {
                if (singleton == null) {
                    singleton = new GifDecoderUtils();
                }
            }
        }
        return singleton;
    }

    /**
     * Disc: 使用Android系统自带的Movie解析Gif，有时候会解析失败，失败后使用(但是高性能，不占内存)
     *
     * @param path
     */
    public void setMovieResource(String path) {
        this.path = path;
        if (decoderByMoive(path)) {
            gifDecoder = null;
            firstFrame = null;
        } else {
            mMovie=null;
            setMoiveByGifDecoder(path);
        }
    }

    private boolean decoderByMoive(String path) {
        try {
            byte[] bytes = getGiftBytes(new FileInputStream(path));
            mMovie = Movie.decodeByteArray(bytes, 0, bytes.length);
//            Logger.d("gif>> delay="+ getDelay() +"  getDuration="+ getDuration());
            return mMovie.duration() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (BuildConfig.DEBUG){
                Logger.d("gif,decoderByMoive:error==>"+e.toString());
            }
        }

        return false;
    }

    public void setMoiveByGifDecoder(String path) {
        if (gifDecoder != null) {
            destroyGifDecoder();
        }
        firstFrame = new FrameDecoder(path).getFrame();
        gifDecoder = new GifDecoder();
        gifDecoder.setGifImage(path);
        gifDecoder.start();
    }

    public void destroyGifDecoder() {
        stopDecodeThread();
        if (gifDecoder != null) {
            gifDecoder.destroy();
            gifDecoder = null;
        }
        if (firstFrame != null) Util.recycleBitmaps(firstFrame);
    }

    private void stopDecodeThread() {
        if (gifDecoder != null && gifDecoder.getState() != Thread.State.TERMINATED) {
            gifDecoder.interrupt();
            gifDecoder.destroy();
        }
    }

    /**
     * @return byte[]
     */
    private byte[] getGiftBytes(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;
        try {
            while ((len = is.read(b, 0, 1024)) != -1) {
                baos.write(b, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baos.toByteArray();
    }

    public void onRelease() {
        if (mMovie != null) {
            mMovie = null;
        }else {
            destroyGifDecoder();
        }
    }

    public int getViewWidth() {
        if (mMovie != null) {
            return mMovie.width();
        }
        if (gifDecoder != null && firstFrame != null) {
            return firstFrame.getWidth();
        }

        return 0;
//			GifFrame gifFrame = gifDecoder.getFrame(0);
//			return gifFrame.image.getWidth();

    }

    public int getViewHeight() {
        if (mMovie != null) {
            return mMovie.height();
        }
        if (gifDecoder != null && firstFrame != null) {
            return firstFrame.getHeight();
        }
        return 0;
    }

    public int getDuration() {
        if (mMovie != null) {
            int duration = mMovie.duration();
            if (duration < 0) {
                duration = 0;
            }
            return duration;
        }
        if (gifDecoder != null) {
            return gifDecoder.getDuration();
        }
        return 0;
    }

    /**
     * 如果控件没有绘制，有可能获取到的delay=0
     * @return
     */
    public int getDelay() {
        if (mMovie != null&& !TextUtils.isEmpty(path)) {
            try {
              return DelayDecoder.getDelay(new FileInputStream(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (gifDecoder!=null){
            return gifDecoder.getDelay();
        }
        return 0;
    }

}

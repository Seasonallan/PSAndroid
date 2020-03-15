package com.season.ps.view.gif;

import android.graphics.Canvas;
import android.graphics.Movie;
import com.season.ps.gif.movie.DelayDecoder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MoviePlugin extends GifPlugin {

    private Movie mMovie;
    private String file;
    @Override
    public boolean init(String file) {
        this.file = file;
        try {
            byte[] bytes = getGiftBytes(new FileInputStream(file));
            mMovie = Movie.decodeByteArray(bytes, 0, bytes.length);
            return mMovie.duration() > 0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "movieDecoder";
    }

    @Override
    public void onRelease() {
        if (mMovie != null){
            mMovie = null;
        }
    }


    public void drawCanvasTime(Canvas canvas, int time) {
        if (mMovie != null) {
            mMovie.setTime(time);
            mMovie.draw(canvas, 0,0,null);
        }
    }



    private static final int DEFAULT_MOVIE_DURATION = 1000;
    private int duration = -1;
    @Override
    public int getDuration() {
        if (duration <= 0){
            if (mMovie == null){
                duration = 0;
            }
            duration = mMovie.duration();
            if(duration <= 0){
                duration = DEFAULT_MOVIE_DURATION;
            }
        }
        return duration;
    }

    @Override
    public int getDelay() {
        if (delay < 0){
            try {
                delay = DelayDecoder.getDelay(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return delay;
    }

    int delay = -1;


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
}

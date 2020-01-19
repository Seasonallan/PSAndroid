package com.season.lib.gif.movie;

import java.io.InputStream;
import com.season.lib.gif.frame.GifDecoder;

/**
 * Disc: 解码器GifDecoder提取的获取GIF的delay延迟时间
 * @see GifDecoder
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 18:37
 */
public class DelayDecoder {

    public static int getDelay(InputStream inputStream) {
        int delay = 100;
        try {
            boolean done = false;
            while (!done) {
                int code = inputStream.read();
                switch (code) {
                    case 0x2C: // image separator
                        break;
                    case 0x21: // extension
                        code = inputStream.read();
                        switch (code) {
                            case 0xf9: // graphics control extension
                                inputStream.read(); // block size
                                inputStream.read(); // packed fields

                                int s = inputStream.read();
                                int f = inputStream.read();
                                int t = s | (f << 8);

                                delay = t * 10;
                                if (delay == 0) {
                                    delay = 100;
                                }
                                done = true;
                                break;
                            default: // uninteresting extension
                        }
                        break;
                    case 0x3b: // terminator
                        done = true;
                        break;
                    case 0x00: // bad byte, but keep going and see what happens
                        break;
                    default:
                        break;
                }
            }
            if (null != inputStream)
                inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return delay;
    }

}

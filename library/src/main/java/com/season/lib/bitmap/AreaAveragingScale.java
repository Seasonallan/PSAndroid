package com.season.lib.bitmap;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * 区域平均算法缩放图片，500x500的图，缩放成小图时；若使用安卓提供的api，
 * 无论怎么抗锯齿，缩放出的图片也是带有较明显锯齿的。此缩放效果提升
 * 效率不够，暂时弃用
 */
@Deprecated
public class AreaAveragingScale {
    protected int[] colorArray;
    private int srcWidth;
    private int srcHeight;

    private int destWidth;
    private int destHeight;

    float[] reds;
    float[] greens;
    float[] blues;
    float[] alphas;

    public AreaAveragingScale(Bitmap src) {
        srcWidth = src.getWidth();
        srcHeight = src.getHeight();

        colorArray = new int[srcWidth * srcHeight];
        src.getPixels(colorArray, 0, srcWidth, 0, 0, srcWidth, srcHeight);
        int a, r, g, b;
        for (int y = 0; y < srcHeight; y++) {
            for (int x = 0; x < srcWidth; x++) {
                int index = y * srcWidth + x;
                a = (colorArray[index] >> 24) & 0xff;
                r = (colorArray[index] >> 16) & 0xff;
                g = (colorArray[index] >> 8) & 0xff;
                b = colorArray[index] & 0xff;
                colorArray[index] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    public Bitmap getScaledBitmap(float width, float height) {
        destWidth = (int) width;
        destHeight = (int) height;
        reds = new float[srcWidth];
        greens = new float[srcWidth];
        blues = new float[srcWidth];
        alphas = new float[srcWidth];

        Bitmap bitmap = Bitmap.createBitmap(destWidth, destHeight, Config.ARGB_8888);
        accumPixels(0, 0, srcWidth, srcHeight, bitmap);

        return bitmap;
    }

    private void accumPixels(int x, int y, int w, int h, Bitmap bitmap) {
        int sy = y;
        int syrem = destHeight;
        int dy, dyrem;
        dy = 0;
        dyrem = 0;
        while (sy < y + h) {
            int amty;
            if (dyrem == 0) {
                for (int i = 0; i < srcWidth; i++) {
                    alphas[i] = reds[i] = greens[i] = blues[i] = 0f;
                }
                dyrem = srcHeight;
            }
            if (syrem < dyrem) {
                amty = syrem;
            } else {
                amty = dyrem;
            }
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = srcWidth;
            float a = 0f, r = 0f, g = 0f, b = 0f;
            while (sx < w) {
                if (sxrem == 0) {
                    sxrem = destWidth;

                    a = getAComponent(sx, sy);
                    r = getRComponent(sx, sy);
                    g = getGComponent(sx, sy);
                    b = getBComponent(sx, sy);

                    if (a != 255.0f) {
                        float ascale = a / 255.0f;
                        r *= ascale;
                        g *= ascale;
                        b *= ascale;
                    }
                }
                int amtx;
                if (sxrem < dxrem) {
                    amtx = sxrem;
                } else {
                    amtx = dxrem;
                }
                float mult = ((float) amtx) * amty;
                alphas[dx] += mult * a;
                reds[dx] += mult * r;
                greens[dx] += mult * g;
                blues[dx] += mult * b;
                if ((sxrem -= amtx) == 0) {
                    sx++;
                }
                if ((dxrem -= amtx) == 0) {
                    dx++;
                    dxrem = srcWidth;
                }
            }
            if ((dyrem -= amty) == 0) {
                do {
                    calcRow(dy, bitmap);
                    dy++;
                } while ((syrem -= amty) >= amty && amty == srcHeight);
            } else {
                syrem -= amty;
            }
            if (syrem == 0) {
                syrem = destHeight;
                sy++;
            }
        }
    }

    private void calcRow(int dy, Bitmap bitmap) {

        // Log.e("", "calcRow" + dy);

        float origmult = ((float) srcWidth) * srcHeight;
        for (int x = 0; x < srcWidth; x++) {
            float mult = origmult;
            int a = Math.round(alphas[x] / mult);
            if (a <= 0) {
                a = 0;
            } else if (a >= 255) {
                a = 255;
            } else {
                mult = alphas[x] / 255;
            }
            int r = Math.round(reds[x] / mult);
            int g = Math.round(greens[x] / mult);
            int b = Math.round(blues[x] / mult);

            if (r < 0) {
                r = 0;
            } else if (r > 255) {
                r = 255;
            }
            if (g < 0) {
                g = 0;
            } else if (g > 255) {
                g = 255;
            }
            if (b < 0) {
                b = 0;
            } else if (b > 255) {
                b = 255;
            }
            setPixelColor(bitmap, x, dy, r, g, b, a);
        }
    }

    private void setPixelColor(Bitmap bitmap, int x, int y, int c0, int c1, int c2, int a) {
        int rgbcolor = (a << 24) + (c0 << 16) + (c1 << 8) + c2;
        int position = ((y * srcWidth + x));
        if (position >= colorArray.length){
            return;
        }
        colorArray[position] = rgbcolor;
        if (x >= destWidth || y >= destHeight) {

        } else {
            bitmap.setPixel(x, y, colorArray[((y * srcWidth + x))]);
        }

    }

    // 获得像素点的透明度 A
    private int getAComponent(int x, int y) {
        return (colorArray[((y * srcWidth + x))] & 0xFF000000) >>> 24;
    }

    // 获得像素点的红色值 R
    private int getRComponent(int x, int y) {
        return (colorArray[((y * srcWidth + x))] & 0x00FF0000) >>> 16;
    }

    // 获得像素点的绿色值 G
    private int getGComponent(int x, int y) {
        return (colorArray[((y * srcWidth + x))] & 0x0000FF00) >>> 8;
    }

    // 获得像素点的蓝色值 B
    private int getBComponent(int x, int y) {
        return (colorArray[((y * srcWidth + x))] & 0x000000FF);
    }
}

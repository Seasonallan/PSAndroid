package com.example.lib.dimen;

import android.graphics.Color;

public class ColorUtil {

    /**
     * Color对象转换成字符串
     *
     * @return 16进制颜色字符串
     */
    private static String toHexFromColor(int red, int green, int blue) {
        String r, g, b;
        StringBuilder su = new StringBuilder();
        r = Integer.toHexString(red);
        g = Integer.toHexString(green);
        b = Integer.toHexString(blue);
        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;
        r = r.toUpperCase();
        g = g.toUpperCase();
        b = b.toUpperCase();
        //  su.append("0xFF");
        su.append(r);
        su.append(g);
        su.append(b);
        //0xFF0000FF
        return su.toString();
    }

    public static String getColorStr(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return toHexFromColor(red, green, blue);
    }

    public static int getColor(String textcolor, int defaultColor) {
        try {
            if (textcolor.startsWith("#")) {
                return Color.parseColor(textcolor);
            } else {
                try {
                    return Color.parseColor("#" + textcolor);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Integer.parseInt(textcolor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defaultColor;
        }
    }
}

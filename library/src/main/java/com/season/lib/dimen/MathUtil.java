package com.season.lib.dimen;


import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

public class MathUtil
{


    /**
     * 获取两点之间的距离
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float getDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float d2 = dx * dx + dy * dy;
        return ((float) Math.sqrt(d2));
    }

    /**
     * 点是否在path区域内
     * @param path
     * @param x
     * @param y
     * @return
     */
    public static boolean isTouchPointInPath(Path path, int x, int y) {
        if (path == null) {
            return false;
        }
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));

        if (region.contains(x, y)) {
            return true;
        }
        return false;
    }


    /**
     * 获取两点之间的夹角
     * @param centerX
     * @param centerY
     * @param xInView
     * @param yInView
     * @return
     */
    public static float getRotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        float rotation = 0;

        float k1 = (centerY - centerY) / (centerX * 2 - centerX);
        float k2 = (yInView - centerY) / (xInView - centerX);
        float tmpDegree = (float) (Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180);

        if (xInView > centerX && yInView < centerY) {
            rotation = 90 - tmpDegree;
        } else if (xInView > centerX && yInView > centerY) {
            rotation = 90 + tmpDegree;
        } else if (xInView < centerX && yInView > centerY) {
            rotation = 270 - tmpDegree;
        } else if (xInView < centerX && yInView < centerY) {
            rotation = 270 + tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        }

        return rotation;
    }

}

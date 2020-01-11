package com.seaon.lib.util;

import android.content.Context;
import android.graphics.Matrix;
import android.util.Log;


public class MathUtil
{
    private static final String TAG = MathUtil.class.getSimpleName();

    public static double pointToLine(double x0, double y0, double x1, double y1, double x2, double y2) {
        double A = y2 - y1;
        double B = x1 - x2;
        double C = x2 * y1 - x1 * y2;
        double d = Math.abs(A * x0 + B * y0 + C) / Math.sqrt(A * A + B * B);
        // double d = (Math.abs((y2 - y1) * x0 + (x1 - x2) * y0 + ((x2 * y1) - (x1 * y2)))) / (Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x1 - x2, 2)));
        return d;
    }

    public static float pointToLine(float x0, float y0, float x1, float y1, float x2, float y2) {
        float A = y2 - y1;
        float B = x1 - x2;
        float C = x2 * y1 - x1 * y2;
        double d = Math.abs(A * x0 + B * y0 + C) / Math.sqrt(A * A + B * B);
        // float d = (Math.abs((y2 - y1) * x0 + (x1 - x2) * y0 + ((x2 * y1) - (x1 * y2)))) / (Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x1 - x2, 2)));
        return ((float) d);
    }

    public static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double d2 = dx * dx + dy * dy;
        return Math.sqrt(d2);
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float d2 = dx * dx + dy * dy;
        return ((float) Math.sqrt(d2));
    }

    public static double getZoom(double cx, double cy, double x1, double y1, double x2, double y2) {
        double d1 = getDistance(cx, cy, x1, y1);
        double d2 = getDistance(cx, cy, x2, y2);
        return d2 / d1;
    }

    public static float getZoom(float cx, float cy, float x1, float y1, float x2, float y2) {
        float d1 = getDistance(cx, cy, x1, y1);
        float d2 = getDistance(cx, cy, x2, y2);
        return d2 / d1;
    }

    public static Matrix getRoateMatrix(float x, float y) {
        return getRoateMatrix(0, 0, x, y);
    }

    public static Matrix getRoateMatrix(float cx, float cy, float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float d = getDistance(cx, cy, x, y);
        float sinA = dy / d;
        float cosA = dx / d;
        Matrix matrix = new Matrix();
        matrix.setValues(new float[]{cosA, -sinA, 0, sinA, cosA, 0, 0, 0, 1});
        return matrix;
    }

    public static float getDegress(float cx, float cy, float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float d = getDistance(cx, cy, x, y);
        float sinA = dy / d;
        float cosA = dx / d;
        double degress = Math.acos(cosA) * 180 / Math.PI;
        if (sinA < 0) {
            degress = -degress;
        }

        //Log.i(TAG, "getDegress: " + degress);
        return ((float) degress);
    }

    public static Matrix getZoomMatrix(float x1, float y1, float x2, float y2) {
        return getZoomMatrix(0, 0, x1, y1, x2, y2);
    }

    public static Matrix getZoomMatrix(float cx, float cy, float x1, float y1, float x2, float y2) {
        float d1 = getDistance(cx, cy, x1, y1);
        float d2 = getDistance(cx, cy, x2, y2);
        float zoom = d2 / d1;
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);
        return matrix;
    }

    public static Matrix getMinZoomMMatrix(Context context, Matrix matrix) {
        return getMinZoomMMatrix(context, matrix, -1, -1);
    }

    public static Matrix getTwoFingerMatrix(Matrix matrix, float lastX1, float lastY1, float lastX2, float lastY2, float x1, float y1, float x2, float y2) {
//        float cx = (lastX1 + lastX2) / 2;
//        float cy = (lastY1 + lastY2) / 2;
//        float d = getDistance(cx, cy, lastX1, lastY1);
//        float d1 = getDistance(cy, cy, x1, y1);
//        float d2 = getDistance(cx, cy, x2, y2);
//        float zoom = d1 * d2 / (d * d);
//        matrix.postScale(zoom, zoom);
//        return matrix;
        float d1 = getDistance(lastX1, lastY1, lastX2, lastY2);
        float d2 = getDistance(x1, y1, x2, y2);
        if (Math.abs(d2 - d1) > 0) {
            float zoom = d2 / d1;
            matrix.postScale(zoom, zoom);
        }

        return matrix;
    }

    public static Matrix getMinZoomMMatrix(Context context, Matrix matrix, float width, float height) {
        Log.i(TAG, "getMinZoomMMatrix: " + matrix.toShortString());
        if (true)
            return matrix;
        float minValue = DimenUtil.dip2px(context,66);

        float minScale = Math.max(minValue / height, minValue / width);
        if (minScale < 0) {
            minScale = 0.8f;
        }
        if (minScale > 1) {
            minScale = 1;
        }
        Log.i(TAG, "getMinZoomMMatrix: minscale=" + minScale);
        //minScale = 0.5f;
        float[] values = new float[9];
        matrix.getValues(values);
        if (values[0] < minScale && values[0] > -minScale) {
            if (values[0] > 0) {
                values[0] = minScale;
            } else {
                values[0] = -minScale;
            }
        }
        if (values[4] < minScale && values[4] > -minScale) {
            if (values[4] > 0) {
                values[4] = minScale;
            } else {
                values[4] = -minScale;
            }
        }
        Matrix matrix1 = new Matrix();
        matrix1.setValues(values);
        return matrix1;
    }

}

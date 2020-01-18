package com.season.lib.ps.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import com.season.lib.ToolBitmapCache;
import com.season.lib.util.Util;
import com.season.lib.util.Logger;
import com.season.lib.util.AutoUtils;


/**
 * Disc: 操作框管理
 *
 * @see PSLayer 图层
 * 重要：图层的scale, translate, rotate信息都在这里。
 * 这里主要包含几个数据数组，1、原始的四角坐标数组srcPoints
 * 2、矩阵操作后的坐标 desPoints
 * 3、矫正的坐标 fixPoints， 矫正的是操作框缩小到最小的时候的数据
 * <p>
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 21:44
 */
public class PSOpView {

    private int padding = 2;
    private Paint bitmapPaint = new Paint();
    private Paint paint = new Paint();

    private Bitmap zoom, scaleX, scaleY, close;
    private Context context;

    private Rect rect;
    public float[] srcPoints, desPoints, fixPoints;
    private int width, height;
    private float minWidth = 108;//最小选中范围正方形边长
    private float[] minScale;
    public boolean isRight = true;

    public PSOpView(Context context) {
        this.context = context;
        desPoints = new float[8];
        fixPoints = new float[8];
        minScale = new float[2];
        padding = (int) (padding * context.getResources().getDisplayMetrics().density);
        minWidth = AutoUtils.getPercentWidthSize(138);//通过影响最小scalex,scaley的临界值，影响到是否校正外面的白色框大小，

        bitmapPaint.setAntiAlias(true);

        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(0xffeeeeee);
        paint.setStrokeWidth(2 * context.getResources().getDisplayMetrics().density);
        try {
            zoom = ToolBitmapCache.getDefault().getZoom(context);
            scaleX = ToolBitmapCache.getDefault().getScaleX(context);
            close = ToolBitmapCache.getDefault().getClose(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 得到操作按钮的宽
     */
    public int getOpViewBitmapWidth() {
        if (zoom != null) {
            return zoom.getWidth();
        }
        return 0;
    }

    public int getPadding() {
        return padding;
    }

    public int getLineWidth() {
        if (paint != null) {
            return (int) paint.getStrokeWidth();
        }
        return 0;
    }

    public int getMinWidth() {
        return (int) minWidth;
    }

    public int getOpviewHeight() {
        //左上，右上，左下，右下
//        canvas.drawLine(fixPoints[0], fixPoints[1], fixPoints[2], fixPoints[3], paint);
//        canvas.drawLine(fixPoints[2], fixPoints[3], fixPoints[4], fixPoints[5], paint);
//        canvas.drawLine(fixPoints[4], fixPoints[5], fixPoints[6], fixPoints[7], paint);
//        canvas.drawLine(fixPoints[6], fixPoints[7], fixPoints[0], fixPoints[1], paint);
//        if (fixPoints!=null&&fixPoints.length==8){
//
//            return (int) (fixPoints[7] - fixPoints[1]);
//        }
        if (rect != null) {
            return (int) (fixPoints[7] - fixPoints[1]);
        }
        return 0;
    }

    public void setPaintColor(int color) {
        paint.setColor(color);
    }

    public void bindRect(ViewGroup parent, Matrix matrix) {
        bindRect(parent, matrix, false);
    }

    public float[] bindRect(ViewGroup parent, Matrix matrix, boolean force) {
        if (srcPoints == null || force) {
            if (parent.getChildCount() > 0) {
                width = parent.getWidth();
                height = parent.getHeight();
                View child = parent.getChildAt(0);
                //四个角的(x,y)坐标
                srcPoints = new float[]{child.getLeft() - padding, child.getTop() - padding, child.getRight() + padding, child
                        .getTop() - padding, child.getRight() + padding, child.getBottom() + padding, child.getLeft() - padding,
                        child.getBottom() + padding};
                rect = new Rect(child.getLeft() - padding, child.getTop() - padding, child.getRight() + padding, child.getBottom() +
                        padding);
                minScale[0] = minWidth / (rect.right - rect.left);
                minScale[1] = minWidth / (rect.bottom - rect.top);
            }
        }
        if (srcPoints == null) {
            return null;
        }
        matrix.mapPoints(desPoints, srcPoints);

        float ox = center[0], oy = center[1];
        //获取到中心点位置 可得到位移X,Y
        center = new float[]{desPoints[0] + (desPoints[4] - desPoints[0]) / 2, desPoints[1] + (desPoints[5] - desPoints[1]) / 2};
        //获取旋转的角度0-360
        degree = PSLayer.getRotationBetweenLines(desPoints[6], desPoints[7], desPoints[0], desPoints[1]);

        double oriX = (srcPoints[2] - srcPoints[0]) * (srcPoints[2] - srcPoints[0]) + (srcPoints[3] - srcPoints[1]) * (srcPoints[3]
                - srcPoints[1]);
        oriX = Math.sqrt(oriX);
        double finX = (desPoints[2] - desPoints[0]) * (desPoints[2] - desPoints[0]) + (desPoints[3] - desPoints[1]) * (desPoints[3]
                - desPoints[1]);
        finX = Math.sqrt(finX);

        double oriY = (srcPoints[6] - srcPoints[0]) * (srcPoints[6] - srcPoints[0]) + (srcPoints[7] - srcPoints[1]) * (srcPoints[7]
                - srcPoints[1]);
        oriY = Math.sqrt(oriY);
        double finY = (desPoints[6] - desPoints[0]) * (desPoints[6] - desPoints[0]) + (desPoints[7] - desPoints[1]) * (desPoints[7]
                - desPoints[1]);
        finY = Math.sqrt(finY);
        //获取放大缩小的倍数
        scale = new float[]{(isRight ? 1 : -1) * (float) (finX / oriX), (float) (finY / oriY)};


        if (Math.abs(scale[0]) >= minScale[0] && scale[1] >= minScale[1]) {
            System.arraycopy(desPoints, 0, fixPoints, 0, 8);
        } else {//如果缩小超过最小scale，fixPoints修正为最小的坐标系

            Matrix matrixFix = new Matrix();
            float sx = (isRight ? 1 : -1) * Math.max(minScale[0], Math.abs(scale[0]));
            float sy = Math.max(minScale[1], scale[1]);
            matrixFix.postTranslate(center[0] - (rect.right - rect.left) / 2 + padding * 1, center[1] - (rect.bottom - rect.top) / 2
                    + padding * 1);
            matrixFix.postScale(sx, sy, center[0], center[1]);
            matrixFix.postRotate(degree, center[0], center[1]);
            matrixFix.mapPoints(fixPoints, srcPoints);
        }


        return new float[]{ox - center[0], oy - center[1]};
    }
    public float getMinScaleY(){
        if (minScale!=null&&minScale.length==2){
            return minScale[1];
        }
        return 0;
    }
    public float[] scale = new float[]{1, 1};
    public float[] center = new float[]{0, 0};
    public float degree;

    public void draw(Canvas canvas, boolean isScale, boolean isZoom, boolean isClose) {
        if (srcPoints == null) {
            return;
        }
        //左上，右上，左下，右下
        canvas.drawLine(fixPoints[0], fixPoints[1], fixPoints[2], fixPoints[3], paint);
        canvas.drawLine(fixPoints[2], fixPoints[3], fixPoints[4], fixPoints[5], paint);
        canvas.drawLine(fixPoints[4], fixPoints[5], fixPoints[6], fixPoints[7], paint);
        canvas.drawLine(fixPoints[6], fixPoints[7], fixPoints[0], fixPoints[1], paint);

        int saveCount = 0;
        if (isScale) {
            float rightCenterX = fixPoints[2] + (fixPoints[4] - fixPoints[2]) / 2;
            float rightCenterY = fixPoints[3] + (fixPoints[5] - fixPoints[3]) / 2;
            if ((rightCenterX < -scaleX.getWidth() && rightCenterY < -scaleX.getHeight()) || (rightCenterX > width + scaleX.getWidth
                    () && rightCenterY < height + scaleX.getHeight())) {

            } else {
                saveCount = canvas.save();
                canvas.rotate(degree, rightCenterX, rightCenterY);
                canvas.drawBitmap(scaleX, rightCenterX - scaleX.getWidth() / 2, rightCenterY - scaleX.getHeight() / 2, bitmapPaint);
                canvas.restoreToCount(saveCount);
            }
        }
        if (isZoom) {
            int positionX = 4, positionY = 5;
            if (!isRight) {
                positionX = 6;
                positionY = 7;
            }
            if ((fixPoints[positionX] < -zoom.getWidth() && fixPoints[positionY] < -zoom.getHeight()) || (fixPoints[positionX] >
                    width + zoom.getWidth() && fixPoints[positionY] < height + zoom.getHeight())) {

            } else {
                saveCount = canvas.save();
                canvas.rotate(degree, fixPoints[positionX], fixPoints[positionY]);
                canvas.drawBitmap(zoom, fixPoints[positionX] - zoom.getWidth() / 2, fixPoints[positionY] - zoom.getHeight() / 2,
                        bitmapPaint);
                canvas.restoreToCount(saveCount);
            }
        }
        if (isClose) {
            int positionX = 0, positionY = 1;
            if (!isRight) {
                positionX = 2;
                positionY = 3;
            }
            if ((fixPoints[positionX] < -close.getWidth() && fixPoints[positionY] < -close.getHeight()) || (fixPoints[positionX] >
                    width + close.getWidth() && fixPoints[positionY] < height + close.getHeight())) {

            } else {
                canvas.drawBitmap(close, fixPoints[positionX] - close.getWidth() / 2, fixPoints[positionY] - close.getHeight() / 2,
                        bitmapPaint);
            }
        }
    }

    /**
     * 是否点击到了删除
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isDeleteTouched(int x, int y) {
        if (fixPoints == null || close == null || close.isRecycled()) {
            return false;
        }
        int positionX = 0, positionY = 1;
        if (!isRight) {
            positionX = 2;
            positionY = 3;
        }
        //加大点击区域
        int radius = close.getWidth();
        if (x >= fixPoints[positionX] - radius && x < fixPoints[positionX] + radius) {
            if (y >= fixPoints[positionY] - radius && y < fixPoints[positionY] + radius) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否点击到了横向缩放
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isScaleTouched(int x, int y) {
        if (fixPoints == null || scaleX == null || scaleX.isRecycled()) {
            return false;
        }
        int radiusX = scaleX.getHeight() / 2;
        int radiusY = scaleX.getHeight() / 2;

        float centerX = (fixPoints[4] + fixPoints[2]) / 2;
        float centerY = (fixPoints[5] + fixPoints[3]) / 2;
        if (x >= centerX - radiusX && x < centerX + radiusX) {
            if (y >= centerY - radiusY && y < centerY + radiusY) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否点击到了拖放旋转
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isRotateTouched(int x, int y) {
        if (fixPoints == null || zoom == null || zoom.isRecycled()) {
            return false;
        }
        int positionX = 4, positionY = 5;
        if (!isRight) {
            positionX = 6;
            positionY = 7;
        }
        int radius = zoom.getWidth() / 2;
        if (x >= fixPoints[positionX] - radius && x < fixPoints[positionX] + radius) {
            if (y >= fixPoints[positionY] - radius && y < fixPoints[positionY] + radius) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否在操作框以内
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isTouched(int x, int y) {
        if (fixPoints == null) {
            return false;
        }

        Path path = new Path();
        path.moveTo(fixPoints[0], fixPoints[1]);
        path.lineTo(fixPoints[2], fixPoints[3]);
        path.lineTo(fixPoints[4], fixPoints[5]);
        path.lineTo(fixPoints[6], fixPoints[7]);
        path.close();

        return Util.isTouchPointInPath(path, x, y);
    }


}

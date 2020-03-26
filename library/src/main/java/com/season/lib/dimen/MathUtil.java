package com.season.lib.dimen;


import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

public class MathUtil
{

    /**
     * 求解直线P1P2和直线P3P4的交点坐标
     */
    public static PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        PointF CrossP = new PointF();
        // 二元函数通式： y=ax+b
        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }

    /**
     * 求pOut在pLine以及pLine2所连直线上的投影点
     *
     * @param pLine
     * @param pLine2
     * @param pOut
     */
    public static PointF getProjectivePoint(PointF pLine, PointF pLine2, PointF pOut) {
        double k = 0;
        try {
            k = getSlope(pLine.x, pLine.y, pLine2.x, pLine2.y);
        } catch (Exception e) {
            k = 0;
        }
        return getProjectivePoint(pLine, k, pOut);
    }

    /**
     * B(t) = (1 - t)^2 * P0 + 2t * (1 - t) * P1 + t^2 * P2, t ∈ [0,1]
     * 曲线的百分比和X轴的百分比不一样，需要进行处理
     * @param t 曲线长度比例
     * @param startPoint 起始点
     * @param controlPoint 控制点
     * @param endPoint 终止点
     * @return t对应的点
     */
    public static PointF calculateBezierPointForQuadratic(float t, PointF startPoint, PointF controlPoint, PointF endPoint) {
        PointF point = new PointF();
        float temp = 1 - t;
        point.x = temp * temp * startPoint.x + 2 * t * temp * controlPoint.x + t * t * endPoint.x;
        point.y = temp * temp * startPoint.y + 2 * t * temp * controlPoint.y + t * t * endPoint.y;
        return point;
    }

    /**
     * 求直线外一点到直线上的投影点
     *
     * @param pLine    线上一点
     * @param k        斜率
     * @param pOut     线外一点
     */
    public static PointF getProjectivePoint(PointF pLine, double k, PointF pOut) {
        PointF pProject = new PointF();
        if (k == 0) {//垂线斜率不存在情况
            pProject.x = pOut.x;
            pProject.y = pLine.y;
        } else {
            pProject.x = (float) ((k * pLine.x + pOut.x / k + pOut.y - pLine.y) / (1 / k + k));
            pProject.y = (float) (-1 / k * (pProject.x - pOut.x) + pOut.y);
        }
        return pProject;
    }
    /**
     * 通过两个点坐标计算斜率
     * 已知A(x1,y1),B(x2,y2)
     * 1、若x1=x2,则斜率不存在；
     * 2、若x1≠x2,则斜率k=[y2－y1]/[x2－x1]
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @throws Exception 如果x1==x2,则抛出该异常
     */
    public static double getSlope(double x1, double y1, double x2, double y2) throws Exception {
        if (x1 == x2) {
            throw new Exception("Slope is not existence,and div by zero!");
        }
        return (y2 - y1) / (x2 - x1);
    }

    // 点到直线的最短距离的判断 点（x0,y0） 到由两点组成的线段（x1,y1） ,( x2,y2 )
    private float pointToLine(float x1, float y1, float x2, float y2, float x0, float y0) {
        float space = 0;
        float a, b, c;
        a = getDistance(x1, y1, x2, y2);// 线段的长度
        b = getDistance(x1, y1, x0, y0);// (x1,y1)到点的距离
        c = getDistance(x2, y2, x0, y0);// (x2,y2)到点的距离
        if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
        }
        if (a <= 0.000001) {
            space = b;
            return space;
        }
        if (c * c >= a * a + b * b) {
            space = b;
            return space;
        }
        if (b * b >= a * a + c * c) {
            space = c;
            return space;
        }
        float p = (a + b + c) / 2;// 半周长
        float s = (float) Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }


    /**
     * 获取两点之间的距离
     * @param point1
     * @param point2
     * @return
     */
    public static float getDistance(PointF point1, PointF point2) {
        float dx = point1.x - point2.x;
        float dy = point1.y - point2.y;
        return (float) Math.hypot(dx,dy);
    }


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
        return (float) Math.hypot(dx,dy);
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

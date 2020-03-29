package com.season.lib.anim;

import android.graphics.PointF;

import com.season.lib.dimen.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻页边缘 贝塞尔曲线 扭曲
 */
public class PointCache {

    int bezierCount = 50;
    float offset = 0.1f;

    private List<PointF> bezierPoints;
    private List<PointF> bezierLinePoints;
    private List<Float> bezierLineLengths;

    public void clear(){
        bezierPoints.clear();
        bezierLinePoints.clear();
        bezierLineLengths.clear();
        bezierLineLengths = null;
        bezierLinePoints = null;
        bezierPoints = null;
    }

    public float getMaxLine(PointF startPoint){
        float maxLength = 0;
        bezierLineLengths = new ArrayList<>();
        for (int i = 0; i< bezierLinePoints.size(); i ++){
            float a = MathUtil.getDistance(startPoint, bezierLinePoints.get(i));// 线段的长度
            maxLength = Math.max(a, maxLength);
            bezierLineLengths.add(0, a);
        }
        return maxLength + offset;
    }

    public void calculateBezierPointForQuadraticList(PointF startPoint, PointF controlPoint, PointF endPoint, PointF touchPoint) {
        bezierPoints = new ArrayList<>();
        bezierLinePoints = new ArrayList<>();
        for (int i = 0; i<= bezierCount; i ++){
            PointF point = MathUtil.calculateBezierPointForQuadratic(i * 1.0f/bezierCount, startPoint, controlPoint, endPoint);
            bezierPoints.add(point);
            bezierLinePoints.add(MathUtil.getProjectivePoint(touchPoint, endPoint, point));
        }
    }

    public float calculateBezierPointForQuadratic(float x){
        int selectedPosition = 0;
        for (int i = 0; i< bezierLineLengths.size(); i ++){
            float length = bezierLineLengths.get(i);
            if (length == x){
                selectedPosition = i;
                break;
            }else{
                if (length > x){
                    selectedPosition = i - 1;
                    break;
                }
            }
        }
        if (selectedPosition <= 0){
            selectedPosition = 0;
        }
        selectedPosition = bezierLineLengths.size() - 1 - selectedPosition;
        return MathUtil.getDistance(bezierPoints.get(selectedPosition), bezierLinePoints.get(selectedPosition)) + offset;
    }

}

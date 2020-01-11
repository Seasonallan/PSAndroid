package com.season.lib.scale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.season.myapplication.BuildConfig;
import com.season.lib.util.Constant;
import com.season.lib.util.MathUtil;
import com.season.lib.util.ScreenUtils;
import com.season.lib.util.ToolPaint;
import com.season.lib.view.ContainerView;
import com.season.lib.view.LayerImageView;
import com.season.lib.view.TextStyleView;
import com.season.lib.gif.frame.GifFrameView;
import com.season.lib.view.GifMovieView;
import com.season.lib.view.IScaleView;
import com.season.lib.util.Logger;


/**
 * Disc: 图层，处理事件用来放大缩小旋转
 * <p>
 * 父类是：
 *
 * @see ContainerView 画布
 * 子类有：
 * @see TextStyleView 文字图层
 * @see LayerImageView 静图图层，包含涂鸦
 * @see GifMovieView gif动图图层
 * @see GifFrameView gif动图图层，只有在GifMovieView解析失败的情况下会用
 * <p>
 * //TODO 未来可以做图层的动效，做法参照回撤的时候的矩阵动画
 * @see MatrixAnimation 矩阵动画
 * <p>
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 21:44
 */
public class ScaleView extends RelativeLayout {




    public ScaleView(Context context) {
        super(context);
        init();
    }

    public ScaleView(Context context, Handler handler) {
        super(context);
        init(handler);
    }

    public ScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public int index = -1;
    public float ZeroValue;

    public void setBackground() {
        index = 0;
    }

    private ScaleDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    public Matrix mCurrentMatrix;
    private OnClickListener mClickListener;
    boolean copy = false;
    public boolean isPreview = false;//是否是预览界面
    public boolean isPreviewEdit = false;//是否是预览界面,编辑文字

    public void bindMatrix(ItemArrayBean item, int opViewWidthEx, int offsetX, int offsetY, float width, float height, boolean scale) {
//        Logger.d("itemArrayBean:  width=" + width+", height="+height +", sizeW="+ item.getSizeWidth() +"  ,sizeH="+item
// .getSizeHeight());
        if (height <= 1) {
            height = width * 4 / 3;
        }
        int opViewWidth = ScreenUtils.getScreenWidth(getContext());
        float centerX = (float) (item.getCenterX() * opViewWidth / width);
        float centerY = (float) ((item.getCenterY() - (height - width) / 2) * opViewWidth / width);
        centerX += offsetX;
        centerY += offsetY;

        if (scale) {
            double transX = centerX - item.getSizeWidth() / 2;
            double transY = centerY - item.getSizeHeight() / 2;
            mCurrentMatrix.postTranslate((float) transX, (float) transY);

            float scaleX = (float) (item.getXScale() * opViewWidth / width);
            mOpView.isRight = !item.isTurnOverH();
            if (!mOpView.isRight) {
                scaleX = -scaleX;
            }
            mCurrentMatrix.postScale(scaleX, (float) (item.getYScale() * opViewWidth / width), centerX, centerY);
        } else {
            double transX = centerX - item.getSizeWidth() * (item.getXScale() * opViewWidth / width) / 2;
            double transY = centerY - item.getSizeHeight() * (item.getYScale() * opViewWidth / width) / 2;

            //Logger.d("transX="+transX +" ,transY="+ transY);
            mCurrentMatrix.postTranslate((float) transX, (float) transY);
        }

        float degree = (float) (item.getAngle() * 180 / Math.PI);//(float) (rotation * Math.PI / 180);
        if (!mOpView.isRight) {
            //degree += 180;
        }
        mCurrentMatrix.postRotate(degree, centerX, centerY);
        copy = true;
    }

    //适配IOS的viewWidth数值， 本来该字段应该是图片的宽高，可是实际IOS存储的图片宽高却是viewwidth * scaleX
    public void bindMatrixImage(ItemArrayBean item, int opViewWidthEx, int offsetX, int offsetY, float width, float height, int
            imageWidth, int imageHeight) {
        if (height <= 1) {
            height = width * 4 / 3;
        }
        int opViewWidth = ScreenUtils.getScreenWidth(getContext());
        float viewWidth = (float) (item.getSizeWidth() * item.getXScale() * opViewWidth / width);
        float viewHeight = (float) (item.getSizeHeight() * item.getYScale() * opViewWidth / width);


        Logger.d("initViewFromData  item.getCenterX()=" + item.getCenterX() + ", opViewWidth=" + opViewWidth + ", width=" + width);

        Logger.LOG(""+ item.getCenterX() +"---->>>>"+ item.getCenterY());
        float centerX = (float) (item.getCenterX() * opViewWidth / width);
        float centerY = (float) ((item.getCenterY() - (height - width) / 2) * opViewWidth / width);
        Logger.LOG(""+ centerX +"---->"+ centerY);
        //TODO PAD需要减掉原来图层宽高差
        centerX += offsetX;
        centerY += offsetY;

        Logger.LOG(""+ offsetX +"----"+ offsetY);


        float transX = centerX - imageWidth / 2;
        float transY = centerY - imageHeight / 2;
        mCurrentMatrix.postTranslate(transX, transY);

        float scaleX = viewWidth / imageWidth * 1.0f;
        float scaleY = viewHeight / imageHeight * 1.0f;

        mOpView.isRight = !item.isTurnOverH();
        if (!mOpView.isRight) {
            scaleX = -scaleX;
        }
        //mOpView.isRight = (item.getXScale() >= 0 ? true:false);
        mCurrentMatrix.postScale(scaleX, scaleY, centerX, centerY);

        Logger.d("initViewFromData  centerX=" + centerX + ", centerY=" + centerY + ", viewWidth=" + viewWidth + "  ,viewHeight=" +
                viewHeight + ", scaleX=" + scaleX + "  ,scaleY=" + scaleY);

        float degree = (float) (item.getAngle() * 180 / Math.PI);//(float) (rotation * Math.PI / 180);
        if (!mOpView.isRight) {
            //degree += 180;
        }
        mCurrentMatrix.postRotate(degree, centerX, centerY);

        copy = true;
    }


    float maxScale = Integer.MAX_VALUE;

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof TextStyleView) {
            maxScale = ((TextStyleView) child).getMaxScale();
            disableHardWareWhenText2Long((TextStyleView) child);
        }
    }

    float scale = 1;

    public void scaleInit(float scale) {
        this.scale = scale;
    }

    public void postScale(float scale, float x, float y) {
        this.mCurrentMatrix.postScale(scale, scale, x, y);
        invalidate();
    }

    /**
     * 文字太长的时候禁止硬件加速，防止无法绘制问题
     */
    public void disableHardWareWhenText2Long(TextStyleView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (view.isText2Long()) {
                Logger.d("硬件disableHardWareWhenText2Long");
                //取消硬件加速
//                this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
    }

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

    boolean isViewOffseted = false;

    public boolean initViewOffset(int parentWidth, int parentHeight) {
        if (isViewOffseted) {
            return false;
        }
        isViewOffseted = true;
        boolean isTuyaAttach = false;
        boolean isDiyBottom4Text = false;
        if (!copy) {
            if (parentWidth > 0) {

                int offsetX = parentWidth / 2;
                int offsetY = parentHeight / 2;
                int halfHText = 0;
                boolean isPreviewBottom = false;
                if (getChildCount() > 0) {
                    View child = getChildAt(0);
                    if (child instanceof LayerImageView) {
                        if (((LayerImageView) child).changeCenter) {
                            offsetX = (int) ((LayerImageView) child).getCenterX();
                            offsetY = (int) ((LayerImageView) child).getCenterY();
                            isTuyaAttach = true;
                        }
                    }
                    if (child instanceof IScaleView) {
                        TextStyleView mText = null;
                        if (child instanceof TextStyleView) {
                            mText = (TextStyleView) child;
                            isDiyBottom4Text = mText.isDiyBottom();
                            isPreviewBottom = mText.isPreViewBottom();
                        }
                        if (isDiyBottom4Text) {
                            //校正文字大小
                            if (getChildAt(0) instanceof TextStyleView) {
                                TextStyleView textStyleView = (TextStyleView) getChildAt(0);
                                int singleLinemaxCount = getSingleLinemaxCount(textStyleView.getText());
                                scale = ToolPaint.getDefault().getScale(getContext(), singleLinemaxCount);
                                float v = textStyleView.getisDiyBottomHeightPercent();
                                halfHText = ((IScaleView) child).getViewHeight() / 2;
                                int offsetY4Diy = textStyleView.getOffsetY4Diy();
                                int lineWidth = 0;
                                if (mOpView != null) {
                                    lineWidth = mOpView.getLineWidth();
                                }
                                offsetY = (int) (parentHeight * v - halfHText * 2);
//                                offsetY = (int) (parentHeight * 1+2*lineWidth);
//                                offsetY = (int) (parentHeight * 1 - scale*halfH);
//                                offsetY = (int) (parentHeight * v - halfH);
//                                offsetY = (int) (parentHeight * v - scale * ((IScaleView) child).getViewHeight() / 2);
                                if (BuildConfig.DEBUG) {
                                    Logger.d("ScaleViewDiyBottom:scale==>" + scale + ",offsetX:" + offsetX + ",offsetY:" + offsetY +
                                            ",halfH:" + halfHText + ",v:" + v+",offsetY4Diy:"+offsetY4Diy);
                                }
                            } else {
                                offsetY = (int) (parentHeight * 3 / 4 - scale * ((IScaleView) child).getViewHeight() / 2);
                            }
                            offsetX = offsetX - ((IScaleView) child).getViewWidth() / 2;
                            if (mText != null) mText.setisDiyBottom(false);
                        } else {
                            offsetX = offsetX - ((IScaleView) child).getViewWidth() / 2;
                            offsetY = offsetY - ((IScaleView) child).getViewHeight() / 2;
                        }
                    } else {
                        offsetX = offsetX - child.getMeasuredWidth() / 2;
                        offsetY = offsetY - child.getMeasuredHeight() / 2;
                    }
                }
                if (!isPreviewBottom) {
                    //TODO
                    float minScaleY = 0.7f;//暂时写死 0.7046263
//                    float minScaleY = mOpView.getMinScaleY();
                    if (BuildConfig.DEBUG) {
                        Logger.d("ScaleViewDiyBottom,isPreviewBottom==>:scale==>" + scale + ",offsetX:" + offsetX + ",offsetY:" +
                                offsetY);
                        Logger.d("ScaleViewDiyBottom,isPreviewBottom==>:parentWidth==>" + parentWidth + ",parentHeight:" +
                                parentHeight+",minScaleY:"+minScaleY);
                    }
                    if (isDiyBottom4Text){
                        mCurrentMatrix.postTranslate(offsetX, offsetY);
                        mCurrentMatrix.postScale(scale, scale, parentWidth / 2, offsetY+halfHText);
                        mCurrentMatrix.postTranslate(0, scale<minScaleY?((1f-minScaleY)/2*2*halfHText):((1f-scale)*halfHText));
//                        ((ScaleView) getParent()).rebindOpView(2*halfHText, scale);
                    }else {
                        mCurrentMatrix.postTranslate(offsetX, offsetY);
                        mCurrentMatrix.postScale(scale, scale, parentWidth / 2, parentHeight / 2);
                    }
                }
            }
        }
        isFocusNow = true;
        //mOpView.bindRect(this, mViewMatrix, true);
        if (getParent() instanceof ContainerView) {
            ((ContainerView) getParent()).addEvent(ContainerView.IType.ADD, this, mCurrentMatrix);
        }
        invalidate();
        if (isTuyaAttach) {
            return false;
        }
        return !copy;
    }

    public boolean isSeekingTo() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof IScaleView && ((IScaleView) view).isSeeking()) {
                return true;
            }
        }
        return false;
    }

    public void refresh() {
        if (matrixAnimation != null && matrixAnimation.isAnimating()) {
            postInvalidate();
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view instanceof IScaleView && ((IScaleView) view).getDuration() > 0) {
                    view.postInvalidate();
                }
            }
        }
    }

    public void record(int time) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof IScaleView && ((IScaleView) view).getDuration() > 0) {
                ((IScaleView) view).recordFrame(time);
            }
        }
    }

    public void recordFinish() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof IScaleView) {
                ((IScaleView) view).stopRecord();
            }
        }
    }

    MatrixAnimation matrixAnimation;

    public void resetMatrix(float[] matrix) {
        matrixAnimation = new MatrixAnimation(mCurrentMatrix, matrix);
        //mCurrentMatrix.setValues(matrix);
        invalidate();
    }

    public void copyMatrix(float[] matrix) {
        mCurrentMatrix.setValues(matrix);
        mCurrentMatrix.postTranslate(20, 20);
        invalidate();
    }


    public void showCenter(int width, int height) {
        mCurrentMatrix = new Matrix();
        mCurrentMatrix.setTranslate(getWidth() / 2 - width / 2, getHeight() / 2 - height / 2);
        invalidate();
    }

    /**
     * 为了显示在预览界面的底部，做了文字大小的缩放操作
     *
     * @param width
     * @param height
     * @param offsetY
     * @param text
     */
    //TODO 减少标点符号的影响
    float defaultScale = 0.5f;

    public float showBottomCenter(int width, int height, int offsetY, String text) {
        value = text;
        int translateY = 0;
        //TODO 放大倍数
        float scale;
        if (TextUtils.isEmpty(value)) {
            scale = defaultScale;
        } else {
            int singleLinemaxCount = getSingleLinemaxCount(text);
            scale = ToolPaint.getDefault().getScale(getContext(), singleLinemaxCount);
            if (BuildConfig.DEBUG) {
                Logger.d("scaleView:" + scale + ",offset:" + (1 - scale) * 0.5f * height);
                Logger.d("scaleView:maxCount" + singleLinemaxCount + "，text:" + text);
            }
        }
        if (mOpView != null) {
//            offsetY =mOpView.getOpViewBitmapWidth()-mOpView.getPadding();
            offsetY = mOpView.getLineWidth();
            float minScaleY = mOpView.getMinScaleY();
//            ((ScaleView) getParent()).rebindOpView(height, scale);后调用补偿位移
//            if (height < mOpView.getMinWidth()) {
//                height = mOpView.getMinWidth();
//            }
//            height = (int) (mOpView.getOpviewHeight()/scale);
            if (BuildConfig.DEBUG) {
                Logger.d("scaleView:offsetY" + offsetY + ",padding:" + mOpView.getPadding());
                Logger.d("scaleView:height" + height + ",mOpView.getMinWidth():" + mOpView.getMinWidth());
                Logger.d("scaleView:minScaleY" + minScaleY);
            }
        }
        translateY = TextUtils.isEmpty(value) ? getHeight() - height * 2 / 3 : getHeight() - height - offsetY;

        mCurrentMatrix.setTranslate(getWidth() / 2 - width / 2, translateY);
        if (BuildConfig.DEBUG) {
            Logger.d("scaleView:" + value + "，translateY：" + translateY);
            Logger.d("scaleView:getHeight():" + getHeight() + ",height:" + height + ",offsetY:" + offsetY);
        }
        int centerX = getWidth() / 2;
        int centerY = getHeight() - height - offsetY + height / 2;
        if (value == null) {
            mCurrentMatrix.postScale(scale, scale, centerX, centerY);
            if (BuildConfig.DEBUG) {
                Logger.d("scaleView:postScale:" + scale);
            }
        } else {
            mCurrentMatrix.postScale(scale, scale, centerX, centerY);
            mCurrentMatrix.postTranslate(0, (1 - scale) * 0.5f * height);
        }
//        9-03 15:26:54.291 30614-30614/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ scaleView:height474,mOpView.getMinWidth():227
//        09-03 15:26:54.291 30614-30614/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ scaleView:左边一个mua~
//                09-03 15:26:54.291 30614-30614/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ scaleView:getHeight():1080,
// height:474,offsetY:6
//        09-03 15:26:54.297 30614-30614/com.biaoqing.BiaoQingShuoShuo D/PRETTY_LOGGER: │ scaleView:rebindOpView486

        invalidate();
        return scale;
    }

    private int getSingleLinemaxCount(String text) {
        int singleLinemaxCount = 0;
        if (text.contains("\n")) {
            String[] split = value.split("\n");
            if (split.length >= 1) {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
//                        String newWithOutPunctuation = s.replaceAll(",.。", "");
                    if (!TextUtils.isEmpty(s)) {
                        if (singleLinemaxCount < s.length()) {
                            singleLinemaxCount = split[i].length();
                        }
                    }
//                        maxCount=(maxCount+newWithOutPunctuation.length())/2;
//                        Logger.d("scaleView:newWithOutPunctuation" + newWithOutPunctuation);
                }
            }
        } else {
            singleLinemaxCount = value.length();
        }
        return singleLinemaxCount;
    }

    public void changeOffset(String text, float x, float y) {
        value = text;
        if (BuildConfig.DEBUG) {
            Logger.d("textstyleview_preview_text_changeOffset,x==>" + x + ",y==>" + y);
        }
        //边界限制
//        if (isPreview) {
//            Logger.d("isPreviewEdit,changeOffset");
//            zeropositionControl(-x, -y);
//        } else {
        mCurrentMatrix.postTranslate(x, y);
//        }
        invalidate();
    }

    public void changeOffsetWithScale(String text, float x, float y) {
        value = text;
        if (BuildConfig.DEBUG) {
            Logger.d("textstyleview_preview_text_changeOffset,x==>" + x + ",y==>" + y);
        }
        //边界限制
//        if (isPreview) {
//            Logger.d("isPreviewEdit,changeOffset");
//            zeropositionControl(-x, -y);
//        } else {
        float scale = 1;
        if (!TextUtils.isEmpty(text)) {
            int singleLinemaxCount = getSingleLinemaxCount(text);
            scale = ToolPaint.getDefault().getScale(getContext(), singleLinemaxCount);
            mCurrentMatrix.postScale(scale, scale, getOffset()[0], getOffset()[1]);
        }
        mCurrentMatrix.postTranslate(x, y);
//        }
        invalidate();
    }

    public float[] rebindOpView() {
        if (mOpView != null) {
            float[] res = mOpView.bindRect(this, mCurrentMatrix, true);
            if (BuildConfig.DEBUG) {
                int height = 0;
                height = mOpView.getOpviewHeight();
                Logger.d("scaleView:rebindOpView" + height);
            }
            float minScaleY = mOpView.getMinScaleY();
            Logger.d("scaleView:rebindOpView,minScaleY:" + minScaleY);
            invalidate();
            return res;
        }
        return null;
    }

    /**
     * 因为opView有最小宽高校正，所以如果对opView的边框显示位置有要求的，需要再次校正位置
     * @param height
     * @param scale
     * @return
     */
    public float[] rebindOpView(int height, float scale) {
        if (mOpView != null) {
            float[] res = mOpView.bindRect(this, mCurrentMatrix, true);
//            if (BuildConfig.DEBUG) {
//                int height = 0;
//                height = mOpView.getOpviewHeight();
//                Logger.d("scaleView:rebindOpView" + height);
//            }
            //opView线条框校正的情况下，需要做位移补偿。
            float minScaleY = mOpView.getMinScaleY();
            Logger.d("scaleView:rebindOpView,minScaleY:" + minScaleY + ",scale:" + scale+",height："+height);
            if (scale < minScaleY) {
                mCurrentMatrix.postTranslate(0, (scale - minScaleY) * 0.5f * height);
            }
            invalidate();
            return res;
        }
        return null;
    }

    boolean isOpEnable = true;

    public void disableOpView() {
        isOpEnable = false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        if (BuildConfig.DEBUG) {
//            canvas.drawColor(getResources().getColor(R.color.colorPrimary));
//        }
        if (matrixAnimation != null && matrixAnimation.isAnimating()) {
            mCurrentMatrix.setValues(matrixAnimation.getValues());
        }
        int saveCount = canvas.save();
        canvas.concat(mCurrentMatrix);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(saveCount);

        if (isOpEnable && getChildCount() > 0 && isFocusNow && value != null) {
            BindRect();
            mOpView.setPaintColor(0xffeeeeee);
            if (isScale) {
                mOpView.draw(canvas, true, false, false);
            } else if (isZoom) {
                mOpView.setPaintColor(!isOffsetDegree ? 0xffeeeeee : 0xff00ff00);
                mOpView.draw(canvas, false, true, false);
            } else if (isOpe) {
                mOpView.draw(canvas, false, false, false);
            } else {
                mOpView.draw(canvas, true, true, true);
            }
        }
    }

    private float offDegree = 0;
    private boolean isOffsetDegree = false;
    public OpView mOpView;

    public void init() {
        ZeroValue = ScreenUtils.getScreenWidth(getContext()) * 1f;
        init(null);
    }

    public void init(Handler handler) {
        //硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // setClipChildren(false);
        mOpView = new OpView(getContext());

        mCurrentMatrix = new Matrix();
        ScaleDetector.OnScaleGestureListener scaleListener = new ScaleDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleDetector detector) {
                isMatrixChanged = true;
                if (value == null) {
                    return true;
                }

                if (center == null) {
                    center = new float[]{getChildAt(0).getWidth() / 2f, getChildAt(0).getHeight() / 2f};
                    mCurrentMatrix.mapPoints(center);
                }

                float preDegree = getRotationBetweenLines(detector.preX2, detector.preY2, detector.preX1, detector.preY1);
                float newDegree = getRotationBetweenLines(detector.currentX2, detector.currentY2, detector.currentX1, detector
                        .currentY1);

                float degree = newDegree - preDegree;
                //  mViewMatrix.postRotate(degree, (detector.preX2 + detector.preX1) / 2, (detector.preY2 + detector.preY1) / 2);
                if (Math.abs(degree) < 18) {
                    mCurrentMatrix.postRotate(degree, center[0], center[1]);
                } else {
                    Logger.d("degree error >> " + degree);
                }

                float scaleFactor = detector.getScaleFactor();
                if (checkScaleOutOfBound(scaleFactor)) {
                    mCurrentMatrix.postScale(scaleFactor, scaleFactor, center[0], center[1]);
                    invalidate();
                }
                return true;
            }

            @Override
            public void onScaleEnd(ScaleDetector detector) {
                super.onScaleEnd(detector);
            }
        };
        mScaleDetector = new ScaleDetector(getContext(), scaleListener, handler);

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mClickListener != null) {
                    mClickListener.onDoubleClick(getChildAt(0));
                }
                return super.onDoubleTap(e);

            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mClickListener != null) {
                    if (simpleTextMode) {
                        if (isDelete) {
                            mClickListener.onDelete(ScaleView.this);
                        } else {
                            mClickListener.onClick(getChildAt(0));
                        }
                        //    isDelete = false;
                        return true;
                    }
                    if (isFocusBefore) {
                        if (isDelete) {
                            mClickListener.onDelete(ScaleView.this);
                        } else if (mClickListener != null) {
                            mClickListener.onClick(getChildAt(0));
                        }
                    }
                }
                //  isDelete = false;
                return true;
            }


            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
                isMatrixChanged = true;
                if (simpleTextMode && value == null) {
                    return true;
                }
                if (true) {
                    if (isZoom) {
                        if (center == null) {
                            center = new float[]{getChildAt(0).getWidth() / 2f, getChildAt(0).getHeight() / 2f};
                            mCurrentMatrix.mapPoints(center);
                        }
                        //   mCurrentScale *= scaleFactor;


                        float preDegree = getRotationBetweenLines(center[0], center[1], currentEvent.getX() + distanceX,
                                currentEvent.getY() + distanceY);
                        float newDegree = getRotationBetweenLines(center[0], center[1], currentEvent.getX(), currentEvent.getY());

                        float degree = newDegree - preDegree;
                        float degreeBefore = mOpView.degree;
                        //正负5度的矫正
                        if (degreeBefore < 355 && degreeBefore > 5 || Math.abs(degree) >= 5) {
                            offDegree = 0;
                            isOffsetDegree = false;
                            mCurrentMatrix.postRotate(degree, center[0], center[1]);
                        } else {
                            if (isOffsetDegree) {
                                offDegree += degree;
                                if (Math.abs(offDegree) >= 5) {
                                    mCurrentMatrix.postRotate(offDegree, center[0], center[1]);
                                    offDegree = 0;
                                    isOffsetDegree = false;
                                }
                            } else {
                                if (degreeBefore + degree >= 355 || degreeBefore + degree <= 5) {
                                    mCurrentMatrix.postRotate(-degreeBefore, center[0], center[1]);
                                    offDegree = 0;
                                    isOffsetDegree = true;
                                }
                            }
                        }


                        float preDistance = MathUtil.getDistance(center[0], center[1], currentEvent.getX() + distanceX, currentEvent
                                .getY() + distanceY);
                        float newDistance = MathUtil.getDistance(center[0], center[1], currentEvent.getX(), currentEvent.getY());
                        //  float scaleFactor = detector.getScaleFactor();

                        float scaleFactor = newDistance / preDistance;
                        if (checkScaleOutOfBound(scaleFactor)) {
                            mCurrentMatrix.postScale(scaleFactor, scaleFactor, center[0], center[1]);
                            invalidate();
                        }
                    } else if (isScale) {
                        if (center == null) {
                            center = new float[]{getChildAt(0).getWidth() / 2f, getChildAt(0).getHeight() / 2f};
                            mCurrentMatrix.mapPoints(center);
                        }

                        float preDistance = MathUtil.getDistance(center[0], center[1], currentEvent.getX() + distanceX, currentEvent
                                .getY() + distanceY);
                        float newDistance = MathUtil.getDistance(center[0], center[1], currentEvent.getX(), currentEvent.getY());
                        //  float scaleFactor = detector.getScaleFactor();

                        float cad = -1f;
                        if (mOpView.isRight) {
                            if (currentEvent.getX() > center[0]) {
                                cad = 1f;
                            } else {
                                cad = -1f;
                                mOpView.isRight = false;
                            }
                        } else {
                            if (currentEvent.getX() > center[0]) {
                                cad = -1f;
                                mOpView.isRight = true;
                            } else {
                                cad = 1f;
                            }
                        }
                        //  mViewMatrix.postRotate(0);
                        mCurrentMatrix.postRotate(-mOpView.degree, center[0], center[1]);
                        mCurrentMatrix.postScale(newDistance / preDistance * cad, 1, center[0], center[1]);
                        mCurrentMatrix.postRotate(mOpView.degree, center[0], center[1]);
                        invalidate();

                    } else {
                        if (getParent() instanceof ContainerView) {
                            ((ContainerView) getParent()).onMove(ScaleView.this, currentEvent);
                        }
                        zeropositionControl(distanceX, distanceY);
                    }
                    //checkBorder();
                }
                return true;
            }
        };
        mGestureDetector = new GestureDetector(getContext(), gestureListener, handler);
    }

    private void zeropositionControl(float distanceX, float distanceY) {
        //对移动范围进行控制，需要得到子类的宽高
        View childAt = null;
        if (getChildCount() > 0) {
            childAt = getChildAt(0);
            float[] floats = new float[9];
            mCurrentMatrix.getValues(floats);
//                        for (int i=0;i<9;i++){
//                            Logger.d("matrix,i:"+floats[i]);
//                        }
            //scale不影响边界数值
            float xscale = floats[0];
            float yscale = floats[4];
            float xtranslate = floats[2];
            float ytranslate = floats[5];
            int width = childAt.getWidth();
            int height = childAt.getHeight();
            //这里代码服务于位置校正，和位移操作
            //预览界面控制中心点不超过边界
            if (distanceY > 0 || (distanceY < 0 && (ytranslate - distanceY) < (ZeroValue - yscale * height / 2))) {
                if (isPreviewEdit) {
                    if (ytranslate > (ZeroValue - yscale * height)) {
                        floats[5] = ZeroValue - yscale * height;
                        floats[2] = xtranslate - distanceX;
                        mCurrentMatrix.setValues(floats);
                    } else {
                        mCurrentMatrix.postTranslate(-distanceX, -distanceY);
                    }
                    isPreviewEdit = false;
                } else {
                    //在边界以上
                    mCurrentMatrix.postTranslate(-distanceX, -distanceY);
                }
            } else {
                if (isPreview) {
                    Logger.d("isPreviewEdit:" + isPreviewEdit);
                    //如果是编辑文字重绘制
                    if (isPreviewEdit) {
                        if (ytranslate > (ZeroValue - yscale * height)) {
                            floats[5] = ZeroValue - yscale * height;
                            floats[2] = xtranslate - distanceX;
                            mCurrentMatrix.setValues(floats);
                        } else {
                            //正常偏移量
                            mCurrentMatrix.postTranslate(-distanceX, -distanceY);
                        }
                        isPreviewEdit = false;
                    } else {
                        //针对预览位移操作
                        mCurrentMatrix.postTranslate(-distanceX, 0);
                    }
                } else {
                    mCurrentMatrix.postTranslate(-distanceX, -distanceY);
                }
            }
            //distanceX，distanceY,左正右负，上正下负
            if (BuildConfig.DEBUG) Logger.d("matrix:" + mCurrentMatrix.toShortString());
//                        Logger.d("distanceX:"+distanceX+",distanceY:"+distanceY);
            invalidate();
        }
    }

    private boolean checkScaleOutOfBound(float scaleFactor) {
        if (scaleFactor > 1) {
            if (mOpView.scale != null && mOpView.scale.length > 0) {
                float scale = mOpView.scale[0];
                if (scale >= maxScale) {
                    invalidate();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isMatrixChanged = false;
    public boolean simpleTextMode = false;
    public String value = "ex";
    float[] center;
    boolean isScale = false, isZoom = false, isOpe = false, isDelete = false;
    public boolean isFocusNow = false, isFocusBefore = false;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            boolean canAttach = true;
            if (getParent() instanceof ContainerView) {
                canAttach = !((ContainerView) getParent()).isEventAttaching();
            }
            if (getChildCount() > 0 && canAttach) {
                center = null;
                BindRect();
                isFocusBefore = isFocusNow;
                isScale = false;
                isZoom = false;
                isDelete = false;
                isFocusNow = mOpView.isTouched((int) ev.getX(), (int) ev.getY());
                if (isFocusBefore && value != null) {
                    isScale = mOpView.isScaleTouched((int) ev.getX(), (int) ev.getY());
                    isZoom = mOpView.isRotateTouched((int) ev.getX(), (int) ev.getY());
                    isDelete = mOpView.isDeleteTouched((int) ev.getX(), (int) ev.getY());
                    if (isScale || isZoom || isDelete) {
                        isFocusNow = true;
                    }
                }
                if (mOnFocusChangeListener != null) {
                    if (isFocusNow) {
                        mOnFocusChangeListener.onFocusGet(this);
                    } else {
                        mOnFocusChangeListener.onFocusLose(this);
                    }
                }
                isOpe = true;
                invalidate();
            }
            if (getParent() instanceof ContainerView) {
                ((ContainerView) getParent()).startOp(this, isFocusNow, true);
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            isOpe = false;
            isScale = false;
            isZoom = false;
            if (getParent() instanceof ContainerView) ((ContainerView) getParent()).onMoveEnd();

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                invalidate();
                if (isInDelRect) {
                    if (mClickListener != null) {
                        mClickListener.onDelete(ScaleView.this);
                    }
                } else {
                    if (isMatrixChanged) {
                        if (getParent() instanceof ContainerView) {
                            ((ContainerView) getParent()).addEvent(ContainerView.IType.MOVE, this, mCurrentMatrix);
                        }
                    }
                }
                isInDelRect = false;
                isMatrixChanged = false;
            }
        }
        if (isScale || isZoom) {
            //手势处理
            mGestureDetector.onTouchEvent(ev);
        } else {
            mScaleDetector.onTouchEvent(ev);
            if (!mScaleDetector.isInProgress()) {
                mGestureDetector.onTouchEvent(ev);
            }
        }
        return isFocusNow;
    }

    public void BindRect() {
        if (mOpView != null) mOpView.bindRect(this, mCurrentMatrix);
    }

    private boolean isInDelRect = false;

    public void setDelPosition(boolean isIn) {
        isInDelRect = isIn;
    }


    private ContainerView.IFocusChangeListener mOnFocusChangeListener;

    public void setFocusChangeListener(ContainerView.IFocusChangeListener listener) {
        mOnFocusChangeListener = listener;
    }

    public void removeFocus() {
        isFocusBefore = false;
        isFocusNow = false;
        invalidate();
    }

    public void getFocus() {
        isFocusBefore = true;
        isFocusNow = true;
        isOpe = false;
        isScale = false;
        isZoom = false;
        invalidate();
    }

    public float[] getOffset() {
        return mOpView.center;
    }


    public ItemArrayBean getItemInfro(int index) {
        ItemArrayBean itemArrayBean = new ItemArrayBean();
        itemArrayBean.setCenterX(mOpView.center[0]);
        itemArrayBean.setCenterY(mOpView.center[1]);

        float rotation = mOpView.degree;
        if (rotation < 0) {
            rotation += 360;
        }
        rotation = (float) (rotation * Math.PI / 180);
        itemArrayBean.setAngle(rotation);
        itemArrayBean.setIndex(index);

        //IOS适配，只有正数
        itemArrayBean.setXScale(Math.abs(mOpView.scale[0]));
        itemArrayBean.setYScale(mOpView.scale[1]);

        if (mOpView.scale[0] < 0) {
            itemArrayBean.setHMoveBtnPositionType(Constant.ToolViewsType.ButtonPositionTypeLeft);
            itemArrayBean.setTurnOverH(true);
        } else {
            itemArrayBean.setHMoveBtnPositionType(Constant.ToolViewsType.ButtonPositionTypeRight);
            itemArrayBean.setTurnOverH(false);
        }

        View view = getChildAt(0);
        //适配IOS的viewWidth数值， 本来该字段应该是图片的宽高，可是实际IOS存储的图片宽高却是viewwidth * scaleX
        itemArrayBean.setSizeWidth(view.getWidth());
        itemArrayBean.setSizeHeight(view.getHeight());
        if (view instanceof TextStyleView) {
            //该字段应该是文字的宽高
            itemArrayBean.setSizeWidth(view.getWidth());
            itemArrayBean.setSizeHeight(view.getHeight());
            itemArrayBean.setContentViewType(Constant.contentViewType.ContentViewTypeTextbox);
            itemArrayBean.setText(((TextStyleView) view).getText());
            itemArrayBean.setTextFontSize(((TextStyleView) view).getTextSize());
           // itemArrayBean.setTextStyleModel(((TextStyleView) view).getTextEntry());
            //itemArrayBean.setImageURL();
            if (((TextStyleView) view).fontName == null) {
                itemArrayBean.setTextFontName("");
            } else {
                itemArrayBean.setTextFontName(((TextStyleView) view).fontName);
            }
        } else if (view instanceof LayerImageView) {
            LayerImageView layerImageView = (LayerImageView) view;
            if (layerImageView.isTuya) {
                itemArrayBean.setContentViewType(Constant.contentViewType.ContentViewTypeDraw);
            } else {
                if (TextUtils.isEmpty(layerImageView.url)) {
                    itemArrayBean.setContentViewType(Constant.contentViewType.ContentViewTypeLocaImage);
                } else {
                    itemArrayBean.setContentViewType(Constant.contentViewType.ContentViewTypeImage);
                }
            }
            itemArrayBean.filePath = layerImageView.filePath;
            itemArrayBean.setImageURL(layerImageView.url);
        } else if (view instanceof GifMovieView) {
            GifMovieView gifMovieView = (GifMovieView) view;
            if (TextUtils.isEmpty(gifMovieView.url)) {
                itemArrayBean.setContentViewType(Constant.contentViewType.ContentViewTypeLocaImage);
            } else {
                itemArrayBean.setContentViewType(Constant.contentViewType.ContentViewTypeImage);
            }
            itemArrayBean.filePath = gifMovieView.file;
            itemArrayBean.setImageURL(gifMovieView.url);
        }
        return itemArrayBean;
    }

    public ScaleView copy() {
        ScaleView scaleView = new ScaleView(getContext());
        View currentview = getChildAt(0);
        if (currentview instanceof LayerImageView) {
            LayerImageView layerImageView = ((LayerImageView) currentview).copy();
            scaleView.addView(layerImageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        if (currentview instanceof TextStyleView) {
            TextStyleView textStyleView = ((TextStyleView) currentview).copy();
            scaleView.addView(textStyleView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        if (currentview instanceof GifMovieView) {
            GifMovieView textStyleView = ((GifMovieView) currentview).copy();
            scaleView.addView(textStyleView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        if (currentview instanceof GifFrameView) {
            GifFrameView textStyleView = ((GifFrameView) currentview).copy();
            scaleView.addView(textStyleView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        float[] matrix = new float[9];
        mCurrentMatrix.getValues(matrix);
        scaleView.copyMatrix(matrix);
        scaleView.copy = true;
        return scaleView;
    }

    public interface OnClickListener {
        void onClick(View view);

        void onDoubleClick(View view);

        void onDelete(View view);
    }

    public void setClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    public boolean isTextView() {
        View currentview = getChildAt(0);
        if (currentview instanceof LayerImageView) {
            return true;
        }
        return false;
    }
}

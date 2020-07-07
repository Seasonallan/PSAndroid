package com.season.ps.view.ps;

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

import com.season.ps.bean.LayerItem;
import com.season.ps.animation.MatrixAnimation;
import com.season.lib.support.math.MathUtil;
import com.season.lib.support.dimen.ScreenUtils;


/**
 * Disc: 图层，处理事件用来放大缩小旋转
 * <p>
 * 父类是：
 *
 * @see PSCanvas 画布
 * 可以放置的视图有：
 * @see CustomTextView 文字图层
 * @see CustomImageView 静图图层，包含涂鸦
 * @see CustomGifView gif动图图层
 * <p>
 * //TODO 未来可以做图层的动效，做法参照回撤的时候的矩阵动画
 * @see MatrixAnimation 矩阵动画
 * <p>
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 21:44
 */
public class PSLayer extends RelativeLayout {

    public PSLayer(Context context) {
        super(context);
        init();
    }

    public PSLayer(Context context, Handler handler) {
        super(context);
        init(handler);
    }

    public PSLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PSLayer(Context context, AttributeSet attrs) {
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

    public void bindMatrix(LayerItem item, int opViewWidthEx, int offsetX, int offsetY, float width, float height, boolean scale) {
//        Logger.d("itemArrayBean:  width=" + width+", height="+height +", sizeW="+ item.getSizeWidth() +"  ,sizeH="+item
// .getSizeHeight());
        if (height <= 1) {
            height = width * 4 / 3;
        }
        int opViewWidth = ScreenUtils.getScreenWidth();
        float centerX = (float) (item.getCenterX() * opViewWidth / width);
        float centerY = (float) ((item.getCenterY() - (height - width) / 2) * opViewWidth / width);
        centerX += offsetX;
        centerY += offsetY;

        if (scale) {
            double transX = centerX - item.getSizeWidth() / 2;
            double transY = centerY - item.getSizeHeight() / 2;
            mCurrentMatrix.postTranslate((float) transX, (float) transY);

            float scaleX = (float) (item.getXScale() * opViewWidth / width);
            mPSOpView.isRight = !item.isTurnOverH();
            if (!mPSOpView.isRight) {
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
        if (!mPSOpView.isRight) {
            //degree += 180;
        }
        mCurrentMatrix.postRotate(degree, centerX, centerY);
        copy = true;
    }

    //适配IOS的viewWidth数值， 本来该字段应该是图片的宽高，可是实际IOS存储的图片宽高却是viewwidth * scaleX
    public void bindMatrixImage(LayerItem item, int opViewWidthEx, int offsetX, int offsetY, float width, float height, int
            imageWidth, int imageHeight) {
        if (height <= 1) {
            height = width * 4 / 3;
        }
        int opViewWidth = ScreenUtils.getScreenWidth();
        float viewWidth = (float) (item.getSizeWidth() * item.getXScale() * opViewWidth / width);
        float viewHeight = (float) (item.getSizeHeight() * item.getYScale() * opViewWidth / width);


        float centerX = (float) (item.getCenterX() * opViewWidth / width);
        float centerY = (float) ((item.getCenterY() - (height - width) / 2) * opViewWidth / width);
        //TODO PAD需要减掉原来图层宽高差
        centerX += offsetX;
        centerY += offsetY;


        float transX = centerX - imageWidth / 2;
        float transY = centerY - imageHeight / 2;
        mCurrentMatrix.postTranslate(transX, transY);

        float scaleX = viewWidth / imageWidth * 1.0f;
        float scaleY = viewHeight / imageHeight * 1.0f;

        mPSOpView.isRight = !item.isTurnOverH();
        if (!mPSOpView.isRight) {
            scaleX = -scaleX;
        }
        //mPSOpView.isRight = (item.getXScale() >= 0 ? true:false);
        mCurrentMatrix.postScale(scaleX, scaleY, centerX, centerY);

        float degree = (float) (item.getAngle() * 180 / Math.PI);//(float) (rotation * Math.PI / 180);
        if (!mPSOpView.isRight) {
            //degree += 180;
        }
        mCurrentMatrix.postRotate(degree, centerX, centerY);

        copy = true;
    }


    float maxScale = Integer.MAX_VALUE;

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof CustomTextView) {
            maxScale = ((CustomTextView) child).getMaxScale();
            disableHardWareWhenText2Long((CustomTextView) child);
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
    public void disableHardWareWhenText2Long(CustomTextView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (view.isText2Long()) {
                //取消硬件加速
                this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
    }


    boolean isViewOffseted = false;

    public boolean initViewOffset(int parentWidth, int parentHeight) {
        if (isViewOffseted) {
            return false;
        }
        isViewOffseted = true;
        if (!copy) {
            if (parentWidth > 0) {
                int offsetX = parentWidth / 2;
                int offsetY = parentHeight / 2;
                if (getChildCount() > 0) {
                    View child = getChildAt(0);
                    if (child instanceof CustomImageView) {
                        if (((CustomImageView) child).changeCenter) {
                            offsetX = (int) ((CustomImageView) child).getCenterX();
                            offsetY = (int) ((CustomImageView) child).getCenterY();
                        }
                    }
                    if (child instanceof ILayer) {
                        offsetX = offsetX - ((ILayer) child).getViewWidth() / 2;
                        offsetY = offsetY - ((ILayer) child).getViewHeight() / 2;
                    } else {
                        offsetX = offsetX - child.getMeasuredWidth() / 2;
                        offsetY = offsetY - child.getMeasuredHeight() / 2;
                    }
                }
                mCurrentMatrix.postTranslate(offsetX, offsetY);
                mCurrentMatrix.postScale(scale, scale, parentWidth / 2, parentHeight / 2);
            }
        }
        isFocusNow = true;
        if (getParent() instanceof PSCanvas) {
            ((PSCanvas) getParent()).addEvent(PSCanvas.IType.ADD, this, mCurrentMatrix);
        }
        invalidate();
        return !copy;
    }

    public void refresh() {
        if (matrixAnimation != null && matrixAnimation.isAnimating()) {
            postInvalidate();
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view instanceof ILayer) {
                    view.postInvalidate();
                }
            }
        }
    }

    public void record(int time, int maxTime) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ILayer) {
                ((ILayer) view).recordFrame(time, maxTime);
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
            scale = ToolPaint.getDefault().getScale(singleLinemaxCount);
        }
        if (mPSOpView != null) {
//            offsetY =mPSOpView.getOpViewBitmapWidth()-mPSOpView.getPadding();
            offsetY = mPSOpView.getLineWidth();
            float minScaleY = mPSOpView.getMinScaleY();
        }
        translateY = TextUtils.isEmpty(value) ? getHeight() - height * 2 / 3 : getHeight() - height - offsetY;

        mCurrentMatrix.setTranslate(getWidth() / 2 - width / 2, translateY);
        int centerX = getWidth() / 2;
        int centerY = getHeight() - height - offsetY + height / 2;
        if (value == null) {
            mCurrentMatrix.postScale(scale, scale, centerX, centerY);
        } else {
            mCurrentMatrix.postScale(scale, scale, centerX, centerY);
            mCurrentMatrix.postTranslate(0, (1 - scale) * 0.5f * height);
        }
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
        //边界限制
        mCurrentMatrix.postTranslate(x, y);
        invalidate();
    }

    public void changeOffsetWithScale(String text, float x, float y) {
        value = text;
        //边界限制
        float scale = 1;
        if (!TextUtils.isEmpty(text)) {
            int singleLinemaxCount = getSingleLinemaxCount(text);
            scale = ToolPaint.getDefault().getScale(singleLinemaxCount);
            mCurrentMatrix.postScale(scale, scale, getOffset()[0], getOffset()[1]);
        }
        mCurrentMatrix.postTranslate(x, y);
        invalidate();
    }

    public float[] rebindOpView() {
        if (mPSOpView != null) {
            float[] res = mPSOpView.bindRect(this, mCurrentMatrix, true);
            float minScaleY = mPSOpView.getMinScaleY();
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
        if (mPSOpView != null) {
            float[] res = mPSOpView.bindRect(this, mCurrentMatrix, true);

            //opView线条框校正的情况下，需要做位移补偿。
            float minScaleY = mPSOpView.getMinScaleY();
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
        if (matrixAnimation != null && matrixAnimation.isAnimating()) {
            mCurrentMatrix.setValues(matrixAnimation.getValues());
        }
        int saveCount = canvas.save();
        canvas.concat(mCurrentMatrix);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(saveCount);

        if (isOpEnable && getChildCount() > 0 && isFocusNow && value != null) {
            BindRect();
            mPSOpView.setPaintColor(0xffeeeeee);
            if (isScale) {
                mPSOpView.draw(canvas, true, false, false);
            } else if (isZoom) {
                mPSOpView.setPaintColor(!isOffsetDegree ? 0xffeeeeee : 0xff00ff00);
                mPSOpView.draw(canvas, false, true, false);
            } else if (isOpe) {
                mPSOpView.draw(canvas, false, false, false);
            } else {
                mPSOpView.draw(canvas, true, true, true);
            }
        }
    }

    private float offDegree = 0;
    private boolean isOffsetDegree = false;
    public PSOpView mPSOpView;

    public void init() {
        ZeroValue = ScreenUtils.getScreenWidth() * 1f;
        init(null);
    }

    public void init(Handler handler) {
        //硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        setClipChildren(false);
        mPSOpView = new PSOpView(getContext());

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

                float preDegree = MathUtil.getRotationBetweenLines(detector.preX2, detector.preY2, detector.preX1, detector.preY1);
                float newDegree = MathUtil.getRotationBetweenLines(detector.currentX2, detector.currentY2, detector.currentX1, detector
                        .currentY1);

                float degree = newDegree - preDegree;
                //  mViewMatrix.postRotate(degree, (detector.preX2 + detector.preX1) / 2, (detector.preY2 + detector.preY1) / 2);
                if (Math.abs(degree) < 18) {
                    mCurrentMatrix.postRotate(degree, center[0], center[1]);
                } else {
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
                            mClickListener.onDelete(PSLayer.this);
                        } else {
                            mClickListener.onClick(getChildAt(0));
                        }
                        //    isDelete = false;
                        return true;
                    }
                    if (isFocusBefore) {
                        if (isDelete) {
                            mClickListener.onDelete(PSLayer.this);
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


                        float preDegree = MathUtil.getRotationBetweenLines(center[0], center[1], currentEvent.getX() + distanceX,
                                currentEvent.getY() + distanceY);
                        float newDegree = MathUtil.getRotationBetweenLines(center[0], center[1], currentEvent.getX(), currentEvent.getY());

                        float degree = newDegree - preDegree;
                        float degreeBefore = mPSOpView.degree;
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
                        if (mPSOpView.isRight) {
                            if (currentEvent.getX() > center[0]) {
                                cad = 1f;
                            } else {
                                cad = -1f;
                                mPSOpView.isRight = false;
                            }
                        } else {
                            if (currentEvent.getX() > center[0]) {
                                cad = -1f;
                                mPSOpView.isRight = true;
                            } else {
                                cad = 1f;
                            }
                        }
                        //  mViewMatrix.postRotate(0);
                        mCurrentMatrix.postRotate(-mPSOpView.degree, center[0], center[1]);
                        mCurrentMatrix.postScale(newDistance / preDistance * cad, 1, center[0], center[1]);
                        mCurrentMatrix.postRotate(mPSOpView.degree, center[0], center[1]);
                        invalidate();

                    } else {
                        if (getParent() instanceof PSCanvas) {
                            ((PSCanvas) getParent()).onMove(PSLayer.this, currentEvent);
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
            float yscale = floats[4];
            float ytranslate = floats[5];
            int height = childAt.getHeight();
            //这里代码服务于位置校正，和位移操作
            //预览界面控制中心点不超过边界
            if (distanceY > 0 || (distanceY < 0 && (ytranslate - distanceY) < (ZeroValue - yscale * height / 2))) {
                mCurrentMatrix.postTranslate(-distanceX, -distanceY);
            } else {
                mCurrentMatrix.postTranslate(-distanceX, -distanceY);
            }
            invalidate();
        }
    }

    private boolean checkScaleOutOfBound(float scaleFactor) {
        if (scaleFactor > 1) {
            if (mPSOpView.scale != null && mPSOpView.scale.length > 0) {
                float scale = mPSOpView.scale[0];
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
            if (getParent() instanceof PSCanvas) {
                canAttach = !((PSCanvas) getParent()).isEventAttaching();
            }
            if (getChildCount() > 0 && canAttach) {
                center = null;
                BindRect();
                isFocusBefore = isFocusNow;
                isScale = false;
                isZoom = false;
                isDelete = false;
                isFocusNow = mPSOpView.isTouched((int) ev.getX(), (int) ev.getY());
                if (isFocusBefore && value != null) {
                    isScale = mPSOpView.isScaleTouched((int) ev.getX(), (int) ev.getY());
                    isZoom = mPSOpView.isRotateTouched((int) ev.getX(), (int) ev.getY());
                    isDelete = mPSOpView.isDeleteTouched((int) ev.getX(), (int) ev.getY());
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
            if (getParent() instanceof PSCanvas) {
                ((PSCanvas) getParent()).startOp(this, isFocusNow, true);
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            isOpe = false;
            isScale = false;
            isZoom = false;
            if (getParent() instanceof PSCanvas) ((PSCanvas) getParent()).onMoveEnd();

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                invalidate();
                if (isInDelRect) {
                    if (mClickListener != null) {
                        mClickListener.onDelete(PSLayer.this);
                    }
                } else {
                    if (isMatrixChanged) {
                        if (getParent() instanceof PSCanvas) {
                            ((PSCanvas) getParent()).addEvent(PSCanvas.IType.MOVE, this, mCurrentMatrix);
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
        if (mPSOpView != null) mPSOpView.bindRect(this, mCurrentMatrix);
    }

    private boolean isInDelRect = false;

    public void setDelPosition(boolean isIn) {
        isInDelRect = isIn;
    }


    private PSCanvas.IFocusChangeListener mOnFocusChangeListener;

    public void setFocusChangeListener(PSCanvas.IFocusChangeListener listener) {
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
        return mPSOpView.center;
    }


    public LayerItem getItemInfro(int index) {
        LayerItem layerItem = new LayerItem();
        layerItem.setCenterX(mPSOpView.center[0]);
        layerItem.setCenterY(mPSOpView.center[1]);

        float rotation = mPSOpView.degree;
        if (rotation < 0) {
            rotation += 360;
        }
        rotation = (float) (rotation * Math.PI / 180);
        layerItem.setAngle(rotation);
        layerItem.setIndex(index);

        //IOS适配，只有正数
        layerItem.setXScale(Math.abs(mPSOpView.scale[0]));
        layerItem.setYScale(mPSOpView.scale[1]);

        if (mPSOpView.scale[0] < 0) {
            layerItem.setTurnOverH(true);
        } else {
            layerItem.setTurnOverH(false);
        }

        View view = getChildAt(0);
        //适配IOS的viewWidth数值， 本来该字段应该是图片的宽高，可是实际IOS存储的图片宽高却是viewwidth * scaleX
        layerItem.setSizeWidth(view.getWidth());
        layerItem.setSizeHeight(view.getHeight());
        if (view instanceof ILayer){
            layerItem.startTime = ((ILayer) view).getStartTime();
            layerItem.endTime = ((ILayer) view).getEndTime();
        }
        if (view instanceof CustomTextView) {
            //该字段应该是文字的宽高
            layerItem.setSizeWidth(view.getWidth());
            layerItem.setSizeHeight(view.getHeight());
            layerItem.setContentViewType(LayerItem.ILayerType.ContentViewTypeTextbox);
            layerItem.setText(((CustomTextView) view).getText());
            layerItem.setTextFontSize(((CustomTextView) view).getTextSize());
           // layerItem.setTextStyleModel(((TextStyleView) view).getTextEntry());
            //layerItem.setImageURL();
            if (((CustomTextView) view).fontName == null) {
                layerItem.setTextFontName("");
            } else {
                layerItem.setTextFontName(((CustomTextView) view).fontName);
            }
            layerItem.animationType = ((CustomTextView) view).currentType;
        }  else if (view instanceof CustomGifView) {
            CustomGifView customGifMovie = (CustomGifView) view;
            if (TextUtils.isEmpty(customGifMovie.url)) {
                layerItem.setContentViewType(LayerItem.ILayerType.ContentViewTypeLocaImage);
            } else {
                layerItem.setContentViewType(LayerItem.ILayerType.ContentViewTypeImage);
            }
            layerItem.filePath = customGifMovie.file;
            layerItem.setImageURL(customGifMovie.url);
        } else if (view instanceof CustomImageView) {
            CustomImageView customImageView = (CustomImageView) view;
            if (customImageView.isTuya) {
                layerItem.setContentViewType(LayerItem.ILayerType.ContentViewTypeDraw);
            } else {
                if (TextUtils.isEmpty(customImageView.url)) {
                    layerItem.setContentViewType(LayerItem.ILayerType.ContentViewTypeLocaImage);
                } else {
                    layerItem.setContentViewType(LayerItem.ILayerType.ContentViewTypeImage);
                }
            }
            layerItem.filePath = customImageView.filePath;
            layerItem.setImageURL(customImageView.url);
        }
        return layerItem;
    }

    public PSLayer copy() {
        PSLayer PSLayer = new PSLayer(getContext());
        View currentview = getChildAt(0);
        if (currentview instanceof CustomImageView) {
            CustomImageView customImageView = ((CustomImageView) currentview).copy();
            PSLayer.addView(customImageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        if (currentview instanceof CustomTextView) {
            CustomTextView customTextView = ((CustomTextView) currentview).copy();
            PSLayer.addView(customTextView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        if (currentview instanceof CustomGifView) {
            CustomGifView textStyleView = ((CustomGifView) currentview).copy();
            PSLayer.addView(textStyleView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT));
        }
        float[] matrix = new float[9];
        mCurrentMatrix.getValues(matrix);
        PSLayer.copyMatrix(matrix);
        PSLayer.copy = true;
        return PSLayer;
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
        if (currentview instanceof CustomImageView) {
            return true;
        }
        return false;
    }
}

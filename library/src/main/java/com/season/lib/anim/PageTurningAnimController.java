package com.season.lib.anim;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.math.MathUtil;
import com.season.lib.util.LogUtil;


/**
 * 仿真翻页动画
 * 贝塞尔曲线原理
 */
public class PageTurningAnimController extends AbsHorGestureAnimController {
	private Integer mFromIndex;
	private Integer mToIndex;
	private int mCornerX = 0; // 拖拽点对应的页脚
	private int mCornerY = 0;
	/**
	 * 拖拽点对应的页脚 
	 */
	private Path mPath0;
	private Path mPath1;
	private PointF mTouch = new PointF(); // 拖拽点
	/**
	 * 贝塞尔曲线起始点
	 */
	private PointF mBezierStart1 = new PointF(); // 
	/**
	 * 贝塞尔曲线控制点
	 */
	private PointF mBezierControl1 = new PointF(); // 
	/**
	 * 贝塞尔曲线顶点
	 */
	private PointF mBeziervertex1 = new PointF(); // 
	/**
	 * 贝塞尔曲线结束点
	 */
	private PointF mBezierEnd1 = new PointF(); // 
	/**
	 * 贝塞尔曲线起始点2
	 */
	private PointF mBezierStart2 = new PointF(); // 
	/**
	 * 贝塞尔曲线控制点2
	 */
	private PointF mBezierControl2 = new PointF();
	/**
	 * 贝塞尔曲线顶点2
	 */
	private PointF mBeziervertex2 = new PointF();
	/**
	 * 贝塞尔曲线结束点2
	 */
	private PointF mBezierEnd2 = new PointF();

	private float mMiddleX;
	private float mMiddleY;
	private float mTouchToCornerDis;
	private Matrix mMatrix;
	private float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };

	private boolean mIsRTandLB = false; // 是否属于右上左下
	private float mMaxLength;
	private int[] mBackShadowColors;
	private int[] mFrontShadowColors;
	private GradientDrawable mBackShadowDrawableLR;
	private GradientDrawable mBackShadowDrawableRL;
	private GradientDrawable mFolderShadowDrawableLR;
	private GradientDrawable mFolderShadowDrawableRL;

	private GradientDrawable mFrontShadowDrawableHBT;
	private GradientDrawable mFrontShadowDrawableHTB;
	private GradientDrawable mFrontShadowDrawableVLR;
	private GradientDrawable mFrontShadowDrawableVRL;
	private int maxShadow = 30;
	private float mRightPageStartX = 0;
	private boolean isCenterTouchAnim;

	PageTurningAnimController() {
		this(null);
	}

	PageTurningAnimController(Interpolator interpolator) {
		super(interpolator);
		createDrawable();
		mPath0 = new Path();
		mPath1 = new Path();
		mMatrix = new Matrix();
	}

	@Override
	protected void setScroller(Scroller scroller, boolean isRequestNext,
			boolean isCancelAnim, PageCarver pageCarver) {
		int dx, dy;
		// dx 水平方向滑动的距离，负值会使滚动向左滚动
		// dy 垂直方向滑动的距离，负值会使滚动向上滚动
		mTouch.set(mLastTouchPoint);
		calcPoints(!isRequestNext);
		if(isCancelAnim){
			if(isRequestNext){
				dx = (int) (mContentWidth - mTouch.x);
			}else{
				dx = (int) - mTouch.x - mContentWidth;
			}
			if (mCornerY > 0) {
				dy = (int) (mContentHeight - mTouch.y);
			} else {
				dy = (int) (1 - mTouch.y); // 防止mTouch.y最终变为0
			}
		}else{
			if (isRequestNext) {
				dx = -(int) (mContentWidth + mTouch.x);
			} else {
				dx = (int) (mContentWidth - mTouch.x + mContentWidth);
			}
			if (mCornerY > 0) {
				dy = (int) (mContentHeight - mTouch.y);
			} else {
				dy = (int) (1 - mTouch.y); // 防止mTouch.y最终变为0
			}
		}
		scrollerDecorator(scroller, (int)mTouch.x, (int)mTouch.y, dx, dy, mDuration);
	}

	@Override
	protected void onMeasure(PageCarver pageCarver) {
		super.onMeasure(pageCarver);
		mMaxLength = (float) Math.hypot(mContentWidth, mContentHeight);
		mRightPageStartX = mContentWidth;
	}

	@Override
	protected void onAnimStart(boolean isCancelAnim) {
	}

	@Override
	protected void onAnimEnd(boolean isCancelAnim) {
		mFromIndex = null;
		mToIndex = null;
		isCenterTouchAnim = false;
	}

	@Override
	protected void onPageStart(float x, float y, PageCarver pageCarver){
		if (!mScrollerStart.isFinished()){
			return;
		}
		if (onTouchDown){
			onTouchDown = false;
			if (isRequestNextPage != null){
				if (isRequestNextPage){
					mScrollerStart.startScroll(mCornerX, mCornerY, (int)x - mCornerX, (int)y - mCornerY , mDuration/3);
				}else{
					mScrollerStart.startScroll(0, 0, (int)x, (int)y , mDuration/3);
				}
				pageCarver.requestInvalidate();
			}
		}else{
			super.onPageStart(x, y , pageCarver);
		}
	}

	@Override
	protected void setDefaultTouchPoint(boolean isNext) {
		if(isNext){
			mDownTouchPoint.x = mContentWidth / 4 * 3;
		}else{
			mDownTouchPoint.x = mContentWidth / 4;
		}
		mDownTouchPoint.y = mContentHeight / 8 * 7;

		//全部展现
		if(isNext){
			mDownTouchPoint.x = mContentWidth -1;
		}else{
			mDownTouchPoint.x = 1;
		}
		mDownTouchPoint.y = mContentHeight - 1;

		mLastTouchPoint.set(mDownTouchPoint);
	}

	@Override
	protected void onRequestPage(boolean isRequestNext,int fromIndex,int toIndex,float x,float y) {
		if(y > mContentHeight / 3f && y < mContentHeight / 3f * 2 || !isRequestNext){
			isCenterTouchAnim = true;
			y = mContentHeight;
		}
		mFromIndex = fromIndex;
		mToIndex = toIndex;
		calcCornerXY(y);
	}

	@Override
	public void dispatchTouchEvent(MotionEvent event, PageCarver pageCarver) {
		if(isCenterTouchAnim && event.getAction() != MotionEvent.ACTION_DOWN){
			event.setLocation(event.getX(), mContentHeight);
		}
		super.dispatchTouchEvent(event, pageCarver);
	}

	@Override
	protected void onDrawAnim(Canvas canvas,boolean isCancelAnim, boolean isNext, PageCarver pageCarver) {
		if(isCenterTouchAnim){
			mLastTouchPoint.y = mContentHeight;
		}
		mTouch.set(mLastTouchPoint);
		int fromIndex = mFromIndex;
		int toIndex = mToIndex;
		if(!isNext){
			fromIndex = mToIndex;
			toIndex = mFromIndex;
			mTouch.x -= mContentWidth / 4;
		}
		calcPoints(!isNext);
		resetPath();

		if (!BitmapUtil.isBitmapAvaliable(cacheBitmap)){
			cacheBitmap = Bitmap.createBitmap(mContentWidth, mContentHeight, Bitmap.Config.RGB_565);
			pageCarver.drawPage(new Canvas(cacheBitmap), fromIndex);
		}

		// 1、绘制当前页面所有区域，包括扭曲部分区域
		drawCurrentPageWarpingArea(canvas, !isNext);

		// 2、绘制翻起页背面//第三个参数表示是否绘制在左边
		drawCurrentBackArea(canvas, fromIndex, !isNext,pageCarver);

		// 3、绘制下一页//第三个参数表示是否绘制在左边
		drawNextPageAreaAndShadow(canvas, toIndex,pageCarver);

		// 4、绘制翻起页的阴影
		drawCurrentPageShadow(canvas);
	}

	private void resetPath() {
		//贝塞尔曲线path
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
				mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
				mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();

		//页脚path
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);

		mPath1.close();
	}

	/**
	 * 绘制当前页面所有区域，包括扭曲部分区域
	 * @param canvas
	 */
	private void drawCurrentPageWarpingArea(Canvas canvas,boolean isLeftPage) {
		int index = 0;
		int WIDTH = 60;
		int HEIGHT = 120;
		int COUNT = (WIDTH + 1) * (HEIGHT + 1);
		float[] verts = new float[COUNT * 2];

		if (isLeftPage || isCenterTouchAnim){
			canvas.save();
			canvas.clipRect(0, 0, mTouch.x, mContentHeight);
			canvas.drawBitmap(cacheBitmap, 0, 0, null);
			canvas.restore();
			return;
		}
		if (mTouch.x > mContentWidth - mContentWidth/WIDTH){
			canvas.drawBitmap(cacheBitmap, 0, 0, null);
			return;
		}

		float rightKeep = (float) Math.hypot(mTouch.x - mBezierEnd1.x,
				mTouch.y - mBezierEnd1.y);
		float xWidth = mContentWidth - rightKeep - mBezierStart1.x;

		for (int y = 0; y <= HEIGHT; y++) {
			float fy = mContentHeight * y  * 1.0f/ HEIGHT;
			for (int x = 0; x <= WIDTH; x++) {
				float fx = mContentWidth * x  * 1.0f/ WIDTH;

				float pointItem = MathUtil.getCrossX(mBezierStart1, mBezierEnd1,  fx, fy);
				if (pointItem <= fx){
					if(!mIsRTandLB){
						float percent = (fx - pointItem)/xWidth;
						percent = percent >= 1? 1: percent;
						float[] pointF = MathUtil.calculateBezierPointForQuadraticFloat(percent, mBezierStart1, mBezierControl1, mBezierEnd1);

						verts[index * 2 + 0] = fx - Math.abs(fx - pointItem -(pointF[0] - mBezierStart1.x));
						verts[index * 2 + 1] = fy - Math.abs(pointF[1] - mBezierStart1.y);
					}else{
						float percent = (fx - pointItem)/xWidth;
						float[] pointF = MathUtil.calculateBezierPointForQuadraticFloat(percent, mBezierStart1, mBezierControl1, mBezierEnd1);

						verts[index * 2 + 0] = fx - Math.abs(fx - pointItem -(pointF[0] - mBezierStart1.x));
						verts[index * 2 + 1] = fy + Math.abs(pointF[1] - mBezierStart1.y);
					}
				}else {
					verts[index * 2 + 0] = fx;
					verts[index * 2 + 1] = fy;
				}
				index++;
			}
		}
		canvas.drawBitmapMesh(cacheBitmap, WIDTH, HEIGHT, verts, 0, null, 0, null);

	}

	/**
	 * 绘制翻起页背面
	 */
	private final void drawCurrentBackArea(Canvas canvas,int pageIndex,boolean isLeftPage, PageCarver pageCarver) {

		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		float dis = (float) Math.hypot(mCornerX - mBezierControl1.x,
				mBezierControl2.y - mCornerY);
		float f8 = (mCornerX - mBezierControl1.x) / dis; //sinX
		float f9 = (mBezierControl2.y - mCornerY) / dis; //cosX
		mMatrixArray[0] = 1 - 2 * f9 * f9; //= 1 - 2(cosX)^2 = -cos2x
		mMatrixArray[1] = 2 * f8 * f9; //= 2sinX cosX = sin2X
		mMatrixArray[3] = mMatrixArray[1];//sin2X
		mMatrixArray[4] = 1 - 2 * f8 * f8;//=1 - 2(sinX)^2 = cos2x
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
		mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);

		canvas.save();
		canvas.concat(mMatrix);

		if (isLeftPage){
			//背景是纯色，直接画，添加蒙版
			pageCarver.drawPage(canvas, pageIndex);
			canvas.drawColor(pageCarver.getPageBackgroundColor() + 0xaa000000);
		} else {
			//背景不是纯色，进行扭曲
			drawCurrentBackWarpingArea(canvas, pageCarver);
		}
		canvas.restore();

		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - maxShadow);
			right = (int) (mBezierStart1.x + f3 + 2);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left = (int) (mBezierStart1.x - f3 - 2);
			right =(int) (mBezierStart1.x + maxShadow + 2);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		float degree = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
				- mCornerX, mBezierControl2.y - mCornerY));
		canvas.rotate(degree, mBezierStart1.x, mBezierStart1.y);
		mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right,
				(int) (mBezierStart1.y + mMaxLength));
		mFolderShadowDrawable.draw(canvas);
		canvas.restore();
	}

	/**
	 * 绘制翻起页背面 扭曲部分
	 */
	private final void drawCurrentBackWarpingArea(Canvas canvas, PageCarver pageCarver) {

		int WIDTH = 20;
		int HEIGHT = 20;
		int COUNT = (WIDTH + 1) * (HEIGHT + 1);  //记录该图片包含21*21个点
		float[] verts = new float[COUNT * 2];    //扭曲前21*21个点的坐标

		int index = 0;

		PointCache pointCacheY = new PointCache();
		pointCacheY.calculateBezierPointForQuadraticList(mBezierStart1, mBezierControl1, mBezierEnd1, mTouch);
		float rightKeep = (float) Math.hypot(mTouch.x - mBezierEnd1.x,
				mTouch.y - mBezierEnd1.y);
		float leftKeep = mContentWidth - pointCacheY.getMaxLine(mBezierEnd1) - rightKeep;

		float bottomKeep = (float) Math.hypot(mTouch.x - mBezierEnd2.x,
				mTouch.y - mBezierEnd2.y);
		PointCache pointCacheX = new PointCache();
		pointCacheX.calculateBezierPointForQuadraticList(mBezierStart2, mBezierControl2, mBezierEnd2, mTouch);
		float topKeep = mContentHeight - pointCacheX.getMaxLine(mBezierEnd2) - bottomKeep;
		for (int y = 0; y <= HEIGHT; y++)
		{
			float fy = mContentHeight * y / HEIGHT;
			for (int x = 0; x <= WIDTH; x++)
			{
				float fx = mContentWidth * x / WIDTH;
				if(!mIsRTandLB){
					float currentY = y * mContentHeight/HEIGHT;
					if (currentY <= topKeep || currentY >= mContentHeight - bottomKeep){
						verts[index * 2 + 0] = fx;
					}else{
						verts[index * 2 + 0] = fx +
								pointCacheX.calculateBezierPointForQuadratic(
										(mContentHeight - bottomKeep - topKeep) - (currentY - topKeep));
					}

					float currentX = x * mContentWidth/WIDTH;
					if (currentX < leftKeep || currentX > mContentWidth - rightKeep){
						verts[index * 2 + 1] = fy ;
					}else{
						verts[index * 2 + 1] = fy +
								pointCacheY.calculateBezierPointForQuadratic(
										(mContentWidth - leftKeep - rightKeep) - (currentX - leftKeep));//* y/HEIGHT;
					}
				}else{
					float currentY = y * mContentHeight/HEIGHT;
					if (currentY <= bottomKeep || currentY >= mContentHeight - topKeep){
						verts[index * 2 + 0] = fx;
					}else{
						verts[index * 2 + 0] = fx +
								pointCacheX.calculateBezierPointForQuadratic(currentY - bottomKeep);
					}
					float currentX = x * mContentWidth/WIDTH;
					if (currentX < leftKeep || currentX > mContentWidth - rightKeep){
						verts[index * 2 + 1] = fy ;
					}else{
						verts[index * 2 + 1] = fy -
								pointCacheY.calculateBezierPointForQuadratic(
										(mContentWidth - leftKeep - rightKeep) - (currentX - leftKeep));//* y/HEIGHT;
					}
				}
				index += 1;
			}
		}
		canvas.drawBitmapMesh(cacheBitmap, WIDTH, HEIGHT, verts, 0, null, 0, null);

		int bgColor = pageCarver.getPageBackgroundColor();
		boolean bgPureColor = bgColor != -1;
		if(bgPureColor){
			canvas.drawColor(bgColor + 0xaa000000);
		}else{
			canvas.drawColor(0xaaffffff);
		}
		pointCacheY.clear();
        pointCacheX.clear();
	}


	/**
	 * 绘制下一页
	 * @param canvas
	 * @param pageIndex
	 * @param pageCarver
	 */
    private final void drawNextPageAreaAndShadow(Canvas canvas,int pageIndex, PageCarver pageCarver) {
		int leftx;
		int rightx;
		GradientDrawable mBackShadowDrawable;
		GradientDrawable mTempShadowDrawableLR = null;
		GradientDrawable mTempShadowDrawableRL = null;

		float touchToCornerDis = mTouchToCornerDis / 4;
		mTempShadowDrawableLR = mBackShadowDrawableLR;
		mTempShadowDrawableRL = mBackShadowDrawableRL;

		if (mIsRTandLB) {
			leftx = (int) (mBezierStart1.x);
			rightx = (int) (mBezierStart1.x + touchToCornerDis);
			mBackShadowDrawable = mTempShadowDrawableLR;
		} else {
			leftx = (int) (mBezierStart1.x - touchToCornerDis);
			rightx = (int) mBezierStart1.x;
			mBackShadowDrawable = mTempShadowDrawableRL;
		}

		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.DIFFERENCE);
		pageCarver.drawPage(canvas, pageIndex);

		float degree = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
				- mCornerX, mBezierControl2.y - mCornerY));
		canvas.rotate(degree, mBezierStart1.x, mBezierStart1.y);
		mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx,
				(int) (mMaxLength + mBezierStart1.y));
		mBackShadowDrawable.draw(canvas);
		canvas.restore();
	}

	private int shadowMaxWidth = 36;
	/**
	 * 绘制翻起页的阴影
	 */
	private final void drawCurrentPageShadow(Canvas canvas) {
		double degree;
		if (mIsRTandLB) {
			degree = Math.PI
					/ 4
					- Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x
							- mBezierControl1.x);
		} else {
			degree = Math.PI
					/ 4
					- Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x
							- mBezierControl1.x);
		}
		// 翻起页阴影顶点与touch点的距离
		double d1 = shadowMaxWidth * 1.5 * Math.cos(degree);
		double d2 = shadowMaxWidth * 1.5 * Math.sin(degree);
		float x = (float) (mTouch.x + d1);
		float y;
		if (mIsRTandLB) {
			y = (float) (mTouch.y + d2);
		} else {
			y = (float) (mTouch.y - d2);
		}
		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		float rotateDegrees;
		canvas.save();

		canvas.clipPath(mPath0, Region.Op.DIFFERENCE);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		int leftx;
		int rightx;
		GradientDrawable mCurrentPageShadow;
		//全屏翻页，边缘阴影宽度渐变
		int shadowWidth = (int) (shadowMaxWidth * (false
				? ( mTouch.x / mContentWidth) : ( (mContentWidth - mTouch.x) / mContentWidth) ) );
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl1.x - 1);
			rightx = (int) mBezierControl1.x + shadowWidth;
			mCurrentPageShadow = mFrontShadowDrawableVLR;
		} else {
			leftx = (int) (mBezierControl1.x - shadowWidth);
			rightx = (int) mBezierControl1.x + 1;
			mCurrentPageShadow = mFrontShadowDrawableVRL;
		}

		rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x
				- mBezierControl1.x, mBezierControl1.y - mTouch.y));
		canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
		mCurrentPageShadow.setBounds(leftx,
				(int) (mBezierControl1.y - mMaxLength), rightx,
				(int) (mBezierControl1.y) +  mContentHeight );
		mCurrentPageShadow.draw(canvas);
		canvas.restore();

		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.DIFFERENCE);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl2.y - 1);
			rightx = (int) (mBezierControl2.y + shadowWidth);
			mCurrentPageShadow = mFrontShadowDrawableHTB;
		} else {
			leftx = (int) (mBezierControl2.y - shadowWidth);
			rightx = (int) (mBezierControl2.y + 1);
			mCurrentPageShadow = mFrontShadowDrawableHBT;
		}
		rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y
				- mTouch.y, mBezierControl2.x - mTouch.x));
		canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
		float temp;
		if (mBezierControl2.y < 0)
			temp = mBezierControl2.y - mContentHeight;
		else
			temp = mBezierControl2.y;

		int hmg = (int) Math.hypot(mBezierControl2.x, temp);
		if (hmg > mMaxLength)
			mCurrentPageShadow
					.setBounds((int) (mBezierControl2.x - shadowMaxWidth) - hmg, leftx,
							(int) (mBezierControl2.x + mMaxLength) - hmg,
							rightx);
		else
			mCurrentPageShadow.setBounds(
					(int) (mBezierControl2.x - mMaxLength), leftx,
					(int) (mBezierControl2.x  +  mContentHeight ), rightx );

		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}

	/**
	 * 约束触屏点可移动范围
	 * @param pointF
	 */
	private final void filterTouchBound(PointF pointF){
		if(pointF.y == pointF.x){
			pointF.y -= 0.1f;
		}else if(mContentWidth - pointF.y == pointF.x){
			pointF.y -= 0.1f;
		}else if(mContentWidth - pointF.x == pointF.y){
			pointF.y -= 0.1f;
		}else if(mContentWidth - pointF.x == mContentHeight - pointF.y){
			pointF.y -= 0.1f;
		}

		if(pointF.y <= 0){
			pointF.y = 0.01f;
		}
		if(pointF.y >= mContentHeight){
			pointF.y = mContentHeight - 0.01f;
		}
	}
	/**
	 * 各个关键点的计算
	 */
	private final void calcPoints(boolean isLeftPage) {
		// 约束触屏点的可移动范围
		filterTouchBound(mTouch);
		mMiddleX = (mTouch.x + mCornerX) / 2;
		mMiddleY = (mTouch.y + mCornerY) / 2;

		mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
				* (mCornerY - mMiddleY) / (mCornerX - mMiddleX);

		mBezierControl1.y = mCornerY;
		mBezierControl2.x = mCornerX;
		mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
				* (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

		mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x)
				/ 2;
		mBezierStart1.y = mCornerY;
		// 在触屏移动时做此约束
		if (!isAnimStart || (mTouch.x > 0 && mTouch.x < mContentWidth)) {
			if (mBezierStart1.x < 0 || mBezierStart1.x > mContentWidth) {if (mBezierStart1.x < 0)
				mBezierStart1.x = mRightPageStartX - mBezierStart1.x;
				float f1 = Math.abs(mCornerX - mTouch.x);
				float f2 = mRightPageStartX * f1 / mBezierStart1.x;
				mTouch.x = Math.abs(mCornerX - f2);

				float f3 = Math.abs(mCornerX - mTouch.x)
						* Math.abs(mCornerY - mTouch.y) / f1;
				mTouch.y = Math.abs(mCornerY - f3);

				mMiddleX = (mTouch.x + mCornerX) / 2;
				mMiddleY = (mTouch.y + mCornerY) / 2;

				mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
						* (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
				mBezierControl1.y = mCornerY;

				mBezierControl2.x = mCornerX;
				mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
						* (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
				mBezierStart1.x = mBezierControl1.x
						- (mCornerX - mBezierControl1.x) / 2;
			}
		}
		mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y)
				/ 2;
		mBezierStart2.x = mCornerX;
		calcTouchToCornerDis();
		mBezierEnd1 = MathUtil.getCross(mTouch, mBezierControl1, mBezierStart1,
				mBezierStart2);
		mBezierEnd2 = MathUtil.getCross(mTouch, mBezierControl2, mBezierStart1,
				mBezierStart2);
		if (mBezierStart1.x > mBezierEnd1.x){
			mBezierEnd1.x = mBezierStart1.x;
		}

		mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
		mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
		mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
		mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;

	}

	private final void calcTouchToCornerDis() {
		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX),
				(mTouch.y - mCornerY));
	}


	/**
	 *  计算拖拽点对应的拖拽脚
	 */
	private final void calcCornerXY(float y) {
		mCornerX = mContentWidth;
		if (y <= mContentHeight / 2)
			mCornerY = 0;
		else
			mCornerY = mContentHeight;
		if ((mCornerX == 0 && mCornerY == mContentHeight)
				|| (mCornerX == mContentWidth && mCornerY == 0))
			mIsRTandLB = true;
		else
			mIsRTandLB = false;
	}
	
	/**
	 *创建阴影的GradientDrawable
	 */
	private final void createDrawable() {
		int[] color = { 0x333333, 0x80333333 };
		mFolderShadowDrawableRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, color);
		mFolderShadowDrawableRL
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFolderShadowDrawableLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, color);
		mFolderShadowDrawableLR
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowColors = new int[] { 0xd0111111, 0x111111 };
		mBackShadowDrawableRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowDrawableLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowColors = new int[] { 0x80111111, 0x111111 };
		mFrontShadowDrawableVLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
		mFrontShadowDrawableVLR
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableVRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
		mFrontShadowDrawableVRL
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHTB = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
		mFrontShadowDrawableHTB
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHBT = new GradientDrawable(
				GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
		mFrontShadowDrawableHBT
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}
}

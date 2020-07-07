package com.season.example.dragview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Scroller;

import com.season.lib.support.bitmap.BitmapUtil;

/**
 * 
/** 
 * 仿Launcher中的WorkSapce，可以左右滑动切换屏幕
 * @author SeasonAllan
 *
 */
public class DragScrollView extends ViewGroup implements IDragListener {
 
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private int mCurScreen;
	private int mDefaultScreen = 0;

	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;

	private static final int SNAP_VELOCITY = 600;

	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX; 

	private PageListener pageListener;
	private DragController mDragController;
	
	private MotionEvent mLongClickEvent = null;
	private PageEdgeController mCountController;
	
	private boolean mDragLock = false;
	
	public DragScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public DragScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mScroller = new Scroller(context);
		
		mCountController = new PageEdgeController(context.getResources().getDisplayMetrics().widthPixels
				, (int) (8 * context.getResources().getDisplayMetrics().density));
		mDragController = DragController.getInstance();
		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		
		DragController.getInstance().registerDragListener(this);
	}

	public interface ICallback<T>{
		DragAdapter<T> getAdapter(List<T> data);
		int getColumnNumber();
		int getLineNumber();
		DragGridView<T> getItemView();
	}

	public <T> void setAdapter(List<T> tList, ICallback<T> callback){
		int numColumn = callback.getColumnNumber(); 
		int numLine = callback.getLineNumber(); 
		int numPage = numColumn * numLine;
		for (int i = 0; i < (tList.size() - 1)/numPage + 1; i++) {
			DragGridView<T> gridView = callback.getItemView();
			if(gridView == null){
				gridView = new DragGridView<T>(getContext());
			}
			List<T> subList = tList.subList(i*numPage, ((i+1)*numPage > tList.size()) ? tList.size():((i+1)*numPage));
			List<T> d = new ArrayList<T>();
			for (int j = 0; j < subList.size(); j++) {
				d.add(subList.get(j));
			} 
			DragAdapter<T> adapter1 = callback.getAdapter(d);
			adapter1.setCurrentPage(i); 
			gridView.setNumColumns(numColumn);
			gridView.setCurrentPageId(i); 
			gridView.setAdapter(adapter1); 
			addView(gridView);
		}
	}

	public <T> void noItemAdd(T item, ICallback<T> callback) {
		if (getChildCount() == 0){

		}else{
			DragGridView itemView = (DragGridView) getChildAt(getChildCount() - 1);
			int itemCount = itemView.getGridAdapter().getCount();
			if (itemCount < callback.getColumnNumber() * callback.getLineNumber()){
				//往当前页面添加
				itemView.getGridAdapter().addItem(itemCount, item);
				snapToScreen(getChildCount() - 1);
				return;
			}
		}
		//添加新的页面
		int numColumn = callback.getColumnNumber();
		DragGridView<T> gridView = callback.getItemView();
		if(gridView == null){
			gridView = new DragGridView<T>(getContext());
		}
		List<T> d = new ArrayList<T>();
		d.add(item);
		DragAdapter<T> adapter1 = callback.getAdapter(d);
		adapter1.setCurrentPage(getChildCount());
		gridView.setNumColumns(numColumn);
		gridView.setCurrentPageId(getChildCount());
		gridView.setAdapter(adapter1);
		addView(gridView);
		snapToScreen(getChildCount() - 1);
	}

	/**
	 * 获取最终数据
	 * @param <T>
	 * @return
	 */
	public <T> List<T> getFinalDatas(){
		List<T> result = new ArrayList<>();
		for (int i = 0; i < getChildCount(); i++){
			DragGridView itemView = (DragGridView) getChildAt(i);
			result.addAll(itemView.getGridAdapter().lstDate);
		}
		return result;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);

		// The children are given the same width and height as the scrollLayout
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen * width, 0);
	}

	/**
	 * According to the position of current layout scroll to the destination
	 * page.
	 */
	public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}

	public void snapToScreen(int whichScreen) {
		// get the valid layout page
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth())) {
			
			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
			mCurScreen = whichScreen;
			if(pageListener != null){
				pageListener.page(mCurScreen);
			}
			invalidate(); // Redraw the layout
		}
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
	}

	/**
	 * 获得当前页码
	 */
	public int getCurScreen() {
		return mCurScreen;
	}
 
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
 
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getRawX();
		final float y = event.getRawY();
 
		switch (action) { 
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (dragImageView != null) { 
				mLongClickEvent = null;
				if(!mDragLock){
					mDragController.notifyDrag(getCurScreen(), event);
					onDrag((int)x, (int)y); 
				}
			}else{
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				if(deltaX <=0 ){
					if(getScrollX() >0 ){
						scrollBy(deltaX, 0);
					}
				}else{
					if(getScrollX() < (getChildCount()- 1)  * getWidth()){
						scrollBy(deltaX, 0);
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: 
			if(mDragController.isDragWorking())
				mDragController.notifyDragDrop(getCurScreen(), event);
			if (dragImageView != null) {
				if (dragImageView != null) {
					dragImageView.setAlpha(60);
					windowManager.removeView(dragImageView);
					dragImageView = null;
				}
			}else{
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
					// Fling enough to move left
					snapToScreen(mCurScreen - 1);
					
				} else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
					// Fling enough to move right
					snapToScreen(mCurScreen + 1);
				} else {
					snapToDestination();
				}
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			break; 
		}
		return true;
	}
 
	@SuppressWarnings("deprecation")
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) { 
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
 
		final float x = ev.getRawX(); 

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			if (xDiff > mTouchSlop || dragImageView != null) { 
				mTouchState = TOUCH_STATE_SCROLLING;
			} 
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			if (dragImageView != null && mLongClickEvent != null) {
				mDragController.notifyDragDrop(getCurScreen(), mLongClickEvent);
				dragImageView.setAlpha(60);
				windowManager.removeView(dragImageView);
				dragImageView = null;
			}else{
				//mDragController.changeStateReady();
			}
			break;
		} 
		return mTouchState != TOUCH_STATE_REST;
	}

	public void setPageListener(PageListener pageListener) {
		this.pageListener = pageListener;
	}

	public interface PageListener {
		void page(int page);
	}
  
	private int mLongClickX,mLongClickY; 

	private int dragStartPointY;
	private int dragStartPointX;

	private int dragOffsetX;
	private int dragOffsetY;
 
	/**
	 * 生成图片过程
	 * @param itemView
	 */
	private void showCreateDragImageAnimation(final ViewGroup itemView){
		itemView.destroyDrawingCache();
		itemView.setDrawingCacheEnabled(true);
		itemView.setDrawingCacheBackgroundColor(0x00000000);
		Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache(true));
		Bitmap orignalBitmp = Bitmap.createBitmap(bm, 0,0, bm.getWidth(), bm.getHeight());
		final Bitmap bitmap = BitmapUtil.scaleBitmpa(orignalBitmp, 1.0f);
		
		if(mDragController.isDragWorking()){
			createBitmapInWindow(bitmap, mLongClickX, mLongClickY);
			getHandler().postDelayed(new Runnable() {
				@Override
				public void run() {
					itemView.setVisibility(View.GONE);
				}
			}, 100);
		} 
	}

	private ImageView dragImageView = null;
	private WindowManager windowManager = null;
	private WindowManager.LayoutParams windowParams = null;
	/**
	 * 创建图片
	 * @param bm
	 * @param x
	 * @param y
	 */
	private void createBitmapInWindow(Bitmap bm, int x, int y) { 
		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP | Gravity.LEFT; 
		windowParams.x = x + dragOffsetX - dragStartPointX;
		windowParams.y = y + dragOffsetY - dragStartPointY;
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.format= PixelFormat.RGBA_8888; //设置图片格式，效果为背景透明
		windowParams.alpha = 0.8f;
		ImageView iv = new ImageView(getContext());
		iv.setImageBitmap(bm);
		 
		windowManager = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		if (dragImageView != null) {
			windowManager.removeView(dragImageView);
		}
		windowManager.addView(iv, windowParams);
		dragImageView = iv;
	} 


	/**
	 * 拖动图片
	 * @param x
	 * @param y
	 */
	private void onDrag(int x, int y) {
		if (dragImageView != null) { 
			mCountController.addCount(x);
			if(mCountController.isAllow2Snap2Next()){
				if (mCurScreen >= getChildCount() -1) {
					mCurScreen = getChildCount() -1;
				}else{ 
					mCurScreen ++;
					snapToScreen(mCurScreen);
					mDragLock = true;
					mDragController.notifyPageSnape(mCurScreen-1, mCurScreen);
				}
			}else if(mCountController.isAllow2Snap2Last()){
				if(mCurScreen <= 0){
					mCurScreen = 0;
				}else{ 
					mCurScreen --;
					snapToScreen(mCurScreen);
					mDragLock = true;
					mDragController.notifyPageSnape(mCurScreen+1, mCurScreen);
				}
			}
			/*y = Math.max(dragStartPointY, y);
			y = Math.min(y, getHeight() - ( - dragStartPointY));
			
			x = Math.max(dragStartPointX, x);
			x = Math.min(x, getWidth() - (halfItemWidth*2 - dragStartPointX));*/
			
			int[] location = new int[2];
			getLocationOnScreen(location);
			
			windowParams.alpha = 0.8f;
			windowParams.x = x + location[0] - dragStartPointX;
			windowParams.y = y + location[1] - dragStartPointY;
 
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
	}
	
	@Override
	public void onDragViewCreate(int page, ViewGroup itemView , MotionEvent event) {
		
		mLongClickEvent = event;
		mLongClickEvent.setAction(MotionEvent.ACTION_DOWN);
		int x = (int)event.getRawX();
		int y = (int)event.getRawY();
		dragStartPointY = y - itemView.getTop();
		dragStartPointX = x - itemView.getLeft();

		int[] location = new int[2];
		getLocationOnScreen(location);
		dragOffsetY = location[1];
		dragOffsetX = location[0];
		
		mLongClickX = (int) event.getRawX();
		mLongClickY = (int) event.getRawY();
		showCreateDragImageAnimation(itemView);
	}

	@Override
	public void onDragViewDestroy(int page, MotionEvent event) {
		mLongClickEvent = null;
	}

	@Override
	public void onItemMove(int page, MotionEvent event) {
		
		
	}

	@Override
	public void onPageChange(int lastPage, int currentPage) {
		
	}

	@Override
	public <T> void onPageChangeRemoveDragItem(int lastPage, int currentPage,
			T object) {
		
		
	}
 
	@Override
	public <T> void onPageChangeReplaceFirstItem(int lastPage,
			int currentPage, T object) {
		
		
	}

	@Override
	public void onPageChangeFinish() {
		mDragLock = false;
	}

	@Override
	public void onDragEnable() {
		
		
	}

	@Override
	public void onDragDisable() {
		
		
	}

	@Override
	public void onItemDelete(int page, int position) {
		mDragController.notifyDeleteItemInPage(getChildCount() -1, getChildCount() -1, page, position, null);
	}

	@Override
	public <T> void onItemDelete(int totalPage, int page, int removePage,  int position, T object) {
		if(totalPage < 0){
			if(page > 0){
				if(getChildCount() > 1)
					snapToScreen(getChildCount() -2);
			}
			removeViewAt(getChildCount() -1);
		}
	}

}



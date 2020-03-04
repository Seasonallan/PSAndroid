package com.season.lib.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 翻页动画控制者
 * @author lyw
 */
public abstract class PageAnimController{
	public static final int ANIM_TYPE_PAGE_TURNING = 0;
	public static final int ANIM_TYPE_TRANSLATION = 1;
	protected Context mContext;

	public static PageAnimController create(Context context,int type){
		return create(context, new AccelerateInterpolator(), type);
	}

	public static PageAnimController create(Context context, Interpolator interpolator,int type){
		PageAnimController pageAnimController = null;
		if(ANIM_TYPE_PAGE_TURNING == type){
			pageAnimController = new PageTurningAnimController(context, interpolator);
		}else if(ANIM_TYPE_TRANSLATION == type){
			pageAnimController = new HorTranslationAnimController(context, interpolator);
		}
		return pageAnimController;
	}

	public abstract void setDuration(int duration);
	
	PageAnimController(Context context) {
		mContext = context;
	}
	
	public Resources getResources(){
		return mContext.getResources();
	}
	/**
	 * 派遣触屏事件
	 * @param event
	 */
	public abstract void dispatchTouchEvent(MotionEvent event,PageCarver pageCarver);
	/**
	 * 派遣绘制事件
	 */
	public abstract boolean dispatchDrawPage(Canvas canvas,PageCarver pageCarver);
	/**
	 * 播放动画
	 */
	public abstract void startAnim(int fromIndex,int toIndex,boolean isNext, PageCarver pageCarver);
	/**
	 * 终止动画
	 */
	public abstract void stopAnim(PageCarver pageCarver);
	/**
	 * 动画是否已经停止,如果返回true就不会再调用dispatchDrawPage方法了
	 * @return
	 */
	public abstract boolean isAnimStop();
	/**
	 * 页绘制者
	 * @author lyw
	 */
	public interface PageCarver{
		/**
		 * 绘制页内容
		 * @param index
		 */
		void drawPage(Canvas canvas,int index);
		/**
		 * 请求翻到上一页
		 * @return
		 */
		Integer requestPrePage();
		/**
		 * 请求翻到下一页
		 * @return
		 */
		Integer requestNextPage();
		/**
		 * 刷新界面
		 */
		void requestInvalidate();
		/**
		 * 获取当前页
		 */
		int getCurrentPageIndex();
		/**
		 * 获取内容区宽度
		 * @return
		 */
		int getContentWidth();
		/**
		 * 获取内容区高度
		 * @return
		 */
		int getContentHeight();
		/**
		 * 获取屏幕宽度
		 * @return
		 */
		int getScreenWidth();
		/**
		 * 获取屏幕高度
		 * @return
		 */
		int getScreenHeight();
		/**
		 * 获取页背景颜色值
		 * @return
		 */
		int getPageBackgroundColor();
		/**
		 * 开始动画的回调
		 * @param isCancel 是否是取消动画
		 */
		void onStartAnim(boolean isCancel);
		/**
		 * 结束动画的回调
		 * @param isCancel 是否是取消动画
		 */
		void onStopAnim(boolean isCancel);
	}
}
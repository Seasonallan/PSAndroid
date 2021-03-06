package com.season.book.plugin.expand.cartoon;


import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 *  漫画视图
 * @author laijp
 * @date 2014-3-25
 * @email 451360508@qq.com
 */
public interface IPageView {
	/**
	 * 设置页与页之间的距离
	 * @param margin
	 */
	public void setPageMargin(int margin);
	
	/**
	 * 设置页面切换监听器
	 * @param listener
	 */
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener);

	/**
	 * 设置页面适配器
	 * @param adapter
	 */
	public void setAdapter(PagerAdapter adapter);

	/**
	 * 设置当前页面
	 * @param index
	 */
	public void setCurrentItem(int index);

	/**
	 * 获取当前页码
	 * @return
	 */
	public int getCurrentItem();
	
	/**
	 * 页面是否是内部缩放
	 * @return
	 */
	public boolean isPhotoView();
	
	/**
	 * 滑动到下一页
	 */
	public void moveToNext();

	/**
	 * 滑动到上一页
	 */
	public void moveToPrevious();
}












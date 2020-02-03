package com.season.example.popwindow;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.example.book.R;
import com.season.example.adapter.BookDigestsItemAdapter;
import com.season.example.adapter.BookmarkItemAdapter;
import com.season.example.adapter.CatalogAdapter;
import com.season.example.adapter.CatalogViewPagerAdapter;
import com.season.lib.bean.BookInfo;
import com.season.lib.bean.Catalog;
import com.season.lib.db.BookDigestsDB;
import com.season.lib.db.BookMarkDB;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.util.SimpleAnimationListener;

public class CatalogView extends FrameLayout{
	private static final String TAG = CatalogView.class.getSimpleName();
	public static final String TAG_CATALOG = "TAG_CATALOG";
	public static final String TAG_DIGEST = "TAG_DIGEST";
	public static final String TAG_BOOKMARK = "TAG_BOOKMARK";

	private Activity mContext;
	private TextView mBookNameTV;
	private TextView mAuthorNameTV;
	private ViewPagerTabHost mTabHost;
	private CatalogAdapter catalogAdapter;
	private CatalogViewPagerAdapter mViewPagerAdapter;
	protected ArrayList<Catalog> mCatalogList;
	private ArrayList<String> mTags = new ArrayList<String>();
	private IActionCallBack mCallBack;
	private boolean isShowing = false;
	private boolean isDismissing = false;
	private View mGotoReaderBut;

	private BookDigestsItemAdapter mBookDigestsAdapter;
	private BookmarkItemAdapter mBookMarkAdapter;
	public CatalogView(final Activity context, IActionCallBack actionCallBack) {
		super(context);
		mContext = context;
		mCallBack = new ActionCallBack(actionCallBack);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.pager_tabs, this, true);
		mBookNameTV = (TextView) findViewById(R.id.catalog_book_name_tv);
		mAuthorNameTV = (TextView) findViewById(R.id.catalog_author_name_tv);
		SlideTabWidget slideTabWidget = (SlideTabWidget) findViewById(android.R.id.tabs);
		slideTabWidget.initialize(LayoutParams.FILL_PARENT,getResources().getDrawable(R.drawable.ic_reader_catalog_select_bg));
		mTabHost = (ViewPagerTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setBackgroundColor(getResources().getColor(R.color.window_bg));
		mTabHost.setup(); 
		mTabHost.setOffscreenPageLimit(2);
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				fillCatalogView(tabId);
			}
		});
		mTabHost.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (position == 2){
					if (positionOffsetPixels >= 0){
						count ++;
						if (count > 20){
							count = 0;
							mCallBack.showReaderContentView();
						}
						return;
					}
				}
				count = 0 ;
			}

			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		mCatalogList = new ArrayList<Catalog>();
		catalogAdapter = new CatalogAdapter(context, mCatalogList);
		mBookDigestsAdapter = new BookDigestsItemAdapter(context);
		mBookDigestsAdapter.setData(BookDigestsDB.getInstance().getListBookDigests(
				actionCallBack.getBookInfo().id));
		mBookMarkAdapter = new BookmarkItemAdapter(context,
				BookMarkDB.getInstance().getUserBookMark(actionCallBack.getBookInfo().id));
		mGotoReaderBut = findViewById(R.id.left_suspension_but);
		mGotoReaderBut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallBack.showReaderContentView();
			}
		});
		mTags.add(TAG_CATALOG);
		mTags.add(TAG_DIGEST);
		mTags.add(TAG_BOOKMARK);
		mViewPagerAdapter = new CatalogViewPagerAdapter(context, mTags){
			public void onItemClicked(String tag, ListView mListView, int position){
				if(tag.equals(CatalogView.TAG_CATALOG)){
					int catalogPosition = mCatalogList.indexOf(mCallBack.getCurrentCatalog());
					if(mListView.getCheckedItemPosition() != catalogPosition){
						mListView.setItemChecked(catalogPosition, true);
						mCallBack.selectCatalog(mCatalogList.get(position));
						mCallBack.showReaderContentView();
					}else{
						mCallBack.showReaderContentView();
					}
				}
			}
			public ListAdapter getAdapter(String tag){
				if(tag.equals(CatalogView.TAG_CATALOG)){
					return catalogAdapter;
				}else if(tag.equals(CatalogView.TAG_DIGEST)){
					return mBookDigestsAdapter;
				}else if(tag.equals(CatalogView.TAG_BOOKMARK)){
					return mBookMarkAdapter;
				}
				return null;
			}
		};
		mTabHost.setAdapter(mViewPagerAdapter);
	}
	int count = 0;
	
	public void setBookInfo(String bookName,String authorName){
		mBookNameTV.setText(bookName);
		mAuthorNameTV.setText(authorName);
	}
	
	public void refreshCatalog(){
		if(catalogAdapter != null){
			catalogAdapter.notifyDataSetInvalidated();
		}
	}
	
	public void setCatalogData(ArrayList<Catalog> catalogs){
		if(catalogs == null){
			return;
		}
		mCatalogList.clear();
		mCatalogList.addAll(catalogs);
		fillCatalogView(mTabHost.getCurrentTabTag());
	}
	
	public void fillCatalogView(String tag){
		fillCatalogView(tag,mTabHost.getTabContentView());
	}
	
	public void fillCatalogView(String tag,View contentView){
		View itemContentView = contentView.findViewWithTag(mViewPagerAdapter.getItemViewTag(mTabHost.getTabIndexByTag(tag)));
		if(itemContentView == null){
			return;
		}
		final CatalogViewPagerAdapter.ViewHolder viewHolder = (CatalogViewPagerAdapter.ViewHolder) itemContentView.getTag(R.layout.reader_catalog_tab_item_lay);
		if(tag.equals(TAG_CATALOG)){
			int catalogPosition = mCatalogList.indexOf(mCallBack.getCurrentCatalog());
			if(catalogPosition > -1 && catalogPosition < mCatalogList.size()){
				viewHolder.mListView.setItemChecked(catalogPosition, true);
				viewHolder.mListView.setSelection(catalogPosition);
			}
			viewHolder.mListViewBG.setImageDrawable(null);
			catalogAdapter.notifyDataSetChanged();
		}
	}

	public boolean isShowing(){
		return isShowing;
	}
	public boolean isDismissing(){
		return isDismissing;
	}

	public void show(){
		if (isShowing){
			return;
		}
		setVisibility(View.VISIBLE);
		isShowing = true;
		//	setCatalogData(mPlugin.getCatalog());
		Animation trans1 = new TranslateAnimation(
				Animation.ABSOLUTE, -ScreenUtils.getScreenWidth(), Animation.ABSOLUTE,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		trans1.setDuration(600);
		trans1.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				setAnimation(null);
				isShowing = false;
			}
		});
		startAnimation(trans1);
	}
	public void dismiss(final SimpleAnimationListener listener){
		if (isDismissing){
			return;
		}
		isDismissing = true;
		setVisibility(View.INVISIBLE);
		Animation trans1 = new TranslateAnimation(Animation.ABSOLUTE,
				0.0f, Animation.ABSOLUTE, -ScreenUtils.getScreenWidth(),
				Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		trans1.setDuration(350);
		trans1.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				isDismissing = false;
				listener.onAnimationEnd(animation);
			}
		});
		startAnimation(trans1);
	}
	
	/**
	 * 对外部操作的回调
	 *
	 */
	private static class ActionCallBack implements IActionCallBack{
		private IActionCallBack mActionCallBack;
		
		public ActionCallBack(IActionCallBack actionCallBack){
			setActionCallBack(actionCallBack);
		}
		
		public void setActionCallBack(IActionCallBack actionCallBack){
			mActionCallBack = actionCallBack;
		}
		
		@Override
		public void reflashCurrentPageBookmark() {
			if(mActionCallBack != null){
				mActionCallBack.reflashCurrentPageBookmark();
			}
		}
		
		@Override
		public Catalog getCurrentCatalog() {
			if(mActionCallBack != null){
				return mActionCallBack.getCurrentCatalog();
			}
			return null;
		}
		
		@Override
		public void showReaderContentView() {
			if(mActionCallBack != null){
				mActionCallBack.showReaderContentView();
			}
		}

		@Override
		public void selectCatalog(Catalog catalog) {
			if(mActionCallBack != null){
				mActionCallBack.selectCatalog(catalog);
			}
		}
		
		@Override
		public boolean isTextSelectHandlEenabled() {
			if(mActionCallBack != null){
				return mActionCallBack.isTextSelectHandlEenabled();
			}
			return false;
		}
		
		@Override
		public boolean isHasNetWork() {
			if(mActionCallBack != null){
				return mActionCallBack.isHasNetWork();
			}
			return false;
		}

		@Override
		public BookInfo getBookInfo() {
			if(mActionCallBack != null){
				return mActionCallBack.getBookInfo();
			}
			return null;
		}

		@Override
		public void onEditModeChange(boolean isEdit) {
			if(mActionCallBack != null){
				mActionCallBack.onEditModeChange(isEdit);
			}
		}
	}
	
	public interface IActionCallBack{
		public void reflashCurrentPageBookmark();
		
		public Catalog getCurrentCatalog();

		public void showReaderContentView();

		public void selectCatalog(Catalog catalog);
		
		public boolean isTextSelectHandlEenabled();
		
		public boolean isHasNetWork();
		
		public BookInfo getBookInfo();
		
		public void onEditModeChange(boolean isEdit);
	}
}

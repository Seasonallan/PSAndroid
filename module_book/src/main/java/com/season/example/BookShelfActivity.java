package com.season.example;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.season.book.R;
import com.season.book.bean.BookInfo;
import com.season.example.dragview.DragAdapter;
import com.season.example.dragview.DragController;
import com.season.example.dragview.DragGridView;
import com.season.example.dragview.DragScrollView;
import com.season.lib.BaseStartPagerActivity;
import com.season.lib.RoutePath;
import com.season.lib.dimen.ScreenUtils;
import com.season.lib.view.LoadingView;

@Route(path= RoutePath.BOOK)
public class BookShelfActivity extends BaseStartPagerActivity {
	private int NUM_COLUMNS = 3;
	private int NUM_LINES = 4;
	private DragScrollView mContainer;  
	private TextView mPageView;
	private LoadingView mLoadingView;
	@Override
	protected int getLayoutId() {
		return R.layout.activity_shelf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//DragController.getInstance().disableDelFunction();
		mPageView = findViewById(R.id.page);
		mContainer = findViewById(R.id.views);
		mLoadingView = findViewById(R.id.loadView);
		mContainer.setPageListener(new DragScrollView.PageListener() {
			@Override
			public void page(int page) {
				mPageView.setText("书架页码： "+(page + 1));
			}
		});
		findViewById(R.id.btn_wifi).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
		param.height = ScreenUtils.getScreenWidth()/3 * 3/2 * 3;
		mContainer.requestLayout();

		BookShelfPreLoader.getInstance().getBookLists(new BookShelfPreLoader.ICallback() {
			@Override
			public void onBookLoaded(List<BookInfo> bookLists) {
				mLoadingView.setVisibility(View.GONE);
				mContainer.setAdapter(bookLists, new DragScrollView.ICallback<BookInfo>() {

					@Override
					public int getColumnNumber() {
						return NUM_COLUMNS;
					}

					@Override
					public DragAdapter<BookInfo> getAdapter(List<BookInfo> data) {
						return new BookShelfAdapter(BookShelfActivity.this, data);
					}

					@Override
					public DragGridView<BookInfo> getItemView() {
						final DragGridView<BookInfo> view = (DragGridView<BookInfo>) LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid, null);
						view.setOnItemClickListener(new AdapterView.OnItemClickListener(){

							@Override
							public void onItemClick(AdapterView<?> parent, View iview, int position, long id) {
								BookInfo item = (BookInfo) view.getGridAdapter().getItem(position);
								BaseBookActivity.open(BookShelfActivity.this, item);
							}
						});
						return view;
					}

					@Override
					public int getLineNumber() {
						return NUM_LINES;
					}
				});
			}
		});
	}


	@Override
	public void onBackPressed() {
		if(DragController.getInstance().cancelDragMode()){
			super.onBackPressed();
		} 
	} 

	@Override
	protected void onDestroy() {
		super.onDestroy();
		BookShelfPreLoader.getInstance().saveLocal(mContainer.getFinalDatas());
		DragController.getInstance().clear();
	}
	

}




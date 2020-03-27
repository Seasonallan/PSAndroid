package com.season.example;

import java.util.List;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.season.book.R;
import com.season.book.bean.BookInfo;
import com.season.example.dragview.DragAdapter;
import com.season.lib.bitmap.ImageLoader;
import com.season.lib.dimen.ScreenUtils;

public class BookShelfAdapter extends DragAdapter<BookInfo> implements OnClickListener {

	private ImageLoader imageLoader;
	private int mItemHeight, mItemWidth;
	public BookShelfAdapter(Context mContext, List<BookInfo> list) {
		super(mContext, list);
		mItemWidth = ScreenUtils.getScreenWidth()/3;
		mItemHeight = mItemWidth *3/2;
		imageLoader = ImageLoader.getInstance(mItemWidth, mItemHeight);
	} 



	
	@Override
	public View getView(int position) {
		
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.griditem_main, null);

		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT , mItemHeight);
		convertView.setLayoutParams(params);

		TextView titleView = convertView.findViewById(R.id.txt_title);
		ImageView iconView = convertView.findViewById(R.id.iv_icon);
		View deleteView = convertView.findViewById(R.id.iv_del);
		deleteView.setVisibility(mDraging?View.VISIBLE:View.GONE);
		deleteView.setTag(position);
		deleteView.setOnClickListener(this);

		BookInfo info = getItem(position);
		if (TextUtils.isEmpty(info.cover)){
			iconView.setImageResource(R.drawable.nocover);
		}else{
			if (info.cover.startsWith("http")){
				imageLoader.setImageViewBitmap(info.cover, null, iconView, R.drawable.nocover);
			}else{
				imageLoader.setImageViewBitmap(null, info.cover, iconView, R.drawable.nocover);
			}
		}
		titleView.setText(info.title);

		return convertView;
	}


	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.iv_del){
			int position = (Integer) v.getTag();
			deleteItemInPage(position);
		}
	}

}














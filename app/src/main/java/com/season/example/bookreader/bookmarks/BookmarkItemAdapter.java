package com.season.example.bookreader.bookmarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.season.myapplication.R;

import java.util.ArrayList;


public class BookmarkItemAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private ArrayList<BookMark> bookmarks;
	private Context mContext;
	public BookmarkItemAdapter(Context context, ArrayList<BookMark> bookmarks){
		super();
        this.mContext = context;
		inflater = LayoutInflater.from(context);
		this.bookmarks = bookmarks;
	}

	@Override
	public int getCount() {
		if(bookmarks != null){
			return bookmarks.size();
		}
		return 0;
	}

	@Override
	public BookMark getItem(int position) {
		if(position < getCount()){
			return bookmarks.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
//		ViewHolder viewHolder;
//		if(convertView == null){
//			convertView = newView();
//			viewHolder = new ViewHolder();
//			viewHolder.nameTV = (TextView) convertView.findViewById(R.id.bookmark_name_tv);
//			viewHolder.timeTV = (TextView) convertView.findViewById(R.id.bookmark_time_tv);
//			viewHolder.TitleTV = (TextView) convertView.findViewById(R.id.bookmark_title_tv);
//			convertView.setTag(viewHolder);
//		}else{
//			viewHolder = (ViewHolder) convertView.getTag();
//		}
//
//		BookMark item = getItem(position);
//        if (ReadSetting.getInstance(mContext).getThemeType() == ReadSetting.THEME_TYPE_NIGHT){
//            viewHolder.TitleTV.setTextColor(mContext.getResources().getColor(R.color.catalog_night_textcolor));
//        }else{
//            viewHolder.TitleTV.setTextColor(mContext.getResources().getColor(R.color.catalog_day_textcolor));
//        }
//		viewHolder.nameTV.setText(item.getBookmarkName().replaceAll( "\\s", "" ));
//		viewHolder.TitleTV.setLines(1);
//		viewHolder.timeTV.setText(CommonUtil.getNowDay(item.getCreateTime()));
//
//        String catalog = item.getChapterName();
//        if(TextUtils.isEmpty(catalog)){
//            catalog = "第"+item.getChapterID() +"章";
//        }
//		viewHolder.TitleTV.setText(catalog);
		return convertView;
	}

	private View newView(){
		return inflater.inflate(R.layout.activity_main, null);
	}
	

	private class ViewHolder {

		public TextView nameTV;
		public TextView timeTV;
		public TextView TitleTV;
		
	}

}

package com.season.example.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.book.R;
import com.season.lib.bean.BookMark;

import java.util.ArrayList;
import java.util.List;


public class BookmarkItemAdapter extends BaseAdapter {

	private ArrayList<BookMark> bookmarks;
	private Context mContext;
	public BookmarkItemAdapter(Context context, List<BookMark> bookmarks){
		super();
        this.mContext = context;
		this.bookmarks = (ArrayList<BookMark>) bookmarks;
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
		ViewHolder viewHolder = null;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.reader_catalog_item_leyue, null);
			viewHolder.titileTV = (TextView)convertView.findViewById(R.id.catalog_title_tv);
			viewHolder.titileIndexTV = (TextView)convertView.findViewById(R.id.catalog_title_index_tv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		BookMark item =  getItem(position);
		String catalog = item.getChapterName();
		if(TextUtils.isEmpty(catalog)){
			catalog = "第"+item.getChapterID() +"章";
		}
		viewHolder.titileIndexTV.setText(String.valueOf(item.getChapterID()));
		viewHolder.titileTV.setText(catalog);
		return convertView;
	}
	

	private class ViewHolder {
		TextView titileTV;
		TextView titileIndexTV;
		
	}

}

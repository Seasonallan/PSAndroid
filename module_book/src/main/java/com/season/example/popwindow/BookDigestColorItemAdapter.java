package com.season.example.popwindow;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.book.R;


public class BookDigestColorItemAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private int[] colors;
	private Activity mContext;
	private int mHight;
	public BookDigestColorItemAdapter(Activity context, int... colors){
		super();
		this.mContext = context;
		inflater = LayoutInflater.from(context);
		this.colors = colors;
		setImaHight();
		
        
	}

	private void setImaHight() {
        int hspac = BookDigestsRemarksDialog.HSPAC;
        mHight = (mContext.getResources().getDisplayMetrics().widthPixels - 2 * 68 - 4 * hspac) / 5;
	}

	@Override
	public int getCount() {
		if(colors != null){
			return colors.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(position < getCount()){
			return colors[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = newView();
			viewHolder = new ViewHolder();
			viewHolder.contentIV = (ImageView) convertView.findViewById(R.id.content_iv);
			viewHolder.selectTV = (ImageView) convertView.findViewById(R.id.select_iv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		int color = colors[position];
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mHight);
		viewHolder.contentIV.setLayoutParams(lp);
		viewHolder.contentIV.setBackgroundColor(color);
		if(selectedColor == color){
			viewHolder.selectTV.setVisibility(View.VISIBLE);
		}else{
			viewHolder.selectTV.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	private View newView(){
		return inflater.inflate(R.layout.pop_digest_item, null);
	}
	public int selectedColor = -1;

	private class ViewHolder {
		public ImageView contentIV;
		public ImageView selectTV;
	}

}


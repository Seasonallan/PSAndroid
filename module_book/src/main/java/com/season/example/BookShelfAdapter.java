package com.season.example;

import java.util.List;
import android.content.Context;
import android.graphics.BitmapFactory;
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
import com.season.lib.dimen.ScreenUtils;

public class BookShelfAdapter extends DragAdapter<BookInfo> implements OnClickListener {
 
	public BookShelfAdapter(Context mContext, List<BookInfo> list) {
		super(mContext, list); 
	} 


	private int mWidthHeight;
	/**
	 * 获取宽高
	 * @return
	 */
	public int getWidthHeight(){
		if(mWidthHeight <=0 ){
			mWidthHeight = ScreenUtils.getScreenWidth()/3 *3/2;
		}
		return mWidthHeight;
	}
	
	@Override
	public View getView(int position) {
		
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.griditem_main, null);

		@SuppressWarnings("deprecation")
		LayoutParams params = new LayoutParams(
				LayoutParams.FILL_PARENT , getWidthHeight());
		convertView.setLayoutParams(params);

		//convertView.findViewById(R.id.g_one).setBackgroundColor(mDraging?0x66000000: 0xff000000);
		convertView.findViewById(R.id.imageView_del).setVisibility(mDraging?View.VISIBLE:View.GONE);
		
		convertView.findViewById(R.id.imageView_del).setTag(position);
		convertView.findViewById(R.id.imageView_del).setOnClickListener(this);
		
		BookInfo info = getItem(position);
		
		ImageView iconView = convertView.findViewById(R.id.imageView_ItemImage);
		if (TextUtils.isEmpty(info.cover)){
			iconView.setImageResource(R.drawable.ic_launcher);
		}else{
			iconView.setImageBitmap(BitmapFactory.decodeFile(info.cover));
		}
		TextView txtAge = convertView.findViewById(R.id.txt_userAge);
		txtAge.setText(info.title);

		return convertView;
	}

	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.imageView_del){
			int position = (Integer) v.getTag();
			deleteItemInPage(position);
		}
	}

}














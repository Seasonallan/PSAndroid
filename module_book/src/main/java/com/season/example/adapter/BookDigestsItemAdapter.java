package com.season.example.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.book.R;
import com.season.lib.bean.BookDigests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class BookDigestsItemAdapter extends BaseAdapter {
    private ArrayList<BookDigests> mBookDigests;
    private Activity mContext;

    public BookDigestsItemAdapter(Activity context){
        super();
        this.mContext = context;
    }

    public void setData(ArrayList<BookDigests> bookDigests){
        Collections.sort(bookDigests, new SortByDate());
        this.mBookDigests = bookDigests;
    }

    public BookDigests remove(int position) {
        return mBookDigests.remove(position);
    }

    @Override
    public int getCount() {
        if(mBookDigests != null){
            return mBookDigests.size();
        }
        return 0;
    }

    @Override
    public BookDigests getItem(int position) {
        if(position < getCount()){
            return mBookDigests.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        BookDigests catalog =  getItem(position);
        viewHolder.titileIndexTV.setText(String.valueOf(catalog.getChaptersId()));
        viewHolder.titileTV.setText(catalog.getContent());
        return convertView;
    }


    private class ViewHolder {
        TextView titileTV;
        TextView titileIndexTV;
    }

    class SortByDate implements Comparator<BookDigests> {
        @Override
        public int compare(BookDigests obj1,BookDigests obj2){
            BookDigests bookDigests1 = obj1;
            BookDigests bookDigests2 = obj2;
            if(bookDigests1.getDate() <= bookDigests2.getDate()) {
                return 1;
            }
            else{
                return -1;
            }
        }
    }
}


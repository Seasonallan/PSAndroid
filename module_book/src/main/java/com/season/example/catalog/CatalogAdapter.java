package com.season.example.catalog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.book.R;
import com.season.lib.dimen.DimenUtil;
import com.season.lib.bean.Catalog;

import java.util.ArrayList;

public class CatalogAdapter extends BaseAdapter {

    public Context mContext;
    public ArrayList<Catalog> mCatalogList;
    public CatalogAdapter(Context context, ArrayList<Catalog> catalogList){
        this.mContext = context;
        this.mCatalogList = catalogList;
    }

    public int selectCatalog = 0;

    @Override
    public int getCount() {
        return mCatalogList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCatalogList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.reader_catalog_item, null);
            viewHolder.titileTV = (TextView)convertView.findViewById(R.id.catalog_title_tv);
            viewHolder.titileIndexTV = (TextView)convertView.findViewById(R.id.catalog_title_index_tv);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Catalog catalog = (Catalog) getItem(position);
        if(catalog.getPageIndex() != null){
            viewHolder.titileIndexTV.setText(String.valueOf(catalog.getPageIndex()));
        }else{
            viewHolder.titileIndexTV.setText("");
        }
        if(catalog.getBgColor() != null){
            viewHolder.titileTV.setBackgroundColor(catalog.getBgColor());
            viewHolder.titileIndexTV.setBackgroundColor(catalog.getBgColor());
        }else{
            viewHolder.titileTV.setBackgroundDrawable(null);
            viewHolder.titileIndexTV.setBackgroundDrawable(null);
        }
        if(catalog.getTextColor() != null){
            viewHolder.titileTV.setTextColor(catalog.getTextColor());
            viewHolder.titileIndexTV.setTextColor(catalog.getTextColor());
        }else{
            viewHolder.titileTV.setTextColor(mContext.getResources().getColor(R.color.common_black_6));
            viewHolder.titileIndexTV.setTextColor(mContext.getResources().getColor(R.color.common_black_6));
        }
        float textSize = DimenUtil.DIPToPX(11);
        for (int i = 1; i < catalog.getLayer(); i++) {
            textSize *= 0.85f;
        }
        int paddingLeft = DimenUtil.DIPToPX(10) * catalog.getLayer();
        viewHolder.titileTV.setPadding(paddingLeft, convertView.getPaddingTop()
                , convertView.getPaddingRight(), convertView.getPaddingBottom());
        viewHolder.titileTV.setText(catalog.getText());
        viewHolder.titileTV.setTextSize(textSize);
        viewHolder.titileTV.setTextColor(position != selectCatalog?mContext.getResources().getColor(R.color.common_black_6):
                mContext.getResources().getColor(R.color.common_blue_4));
        return convertView;
    }

    private class ViewHolder{
        TextView titileTV;
        TextView titileIndexTV;
    }
}
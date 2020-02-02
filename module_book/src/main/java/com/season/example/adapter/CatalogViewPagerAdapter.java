package com.season.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.book.R;
import com.season.example.popwindow.CatalogView;

import java.util.ArrayList;

public abstract class CatalogViewPagerAdapter  extends BaseViewPagerTabHostAdapter {
    private static final String ITEM_VIEW_TAG = "ITEM_VIEW_TAG";

    public ArrayList<String> mTags;
    private Context mContext;

    public CatalogViewPagerAdapter(Context context, ArrayList<String> tags){
        mContext = context;
        mTags = tags;
        if(mTags == null){
            mTags = new ArrayList<String>();
        }
    }

    @Override
    public View getIndicator(int position) {
        View indicatorView = null;
        String tag = getTab(position);
        if(tag.equals(CatalogView.TAG_CATALOG)){
            indicatorView = newIndicator(R.string.btn_text_catalog);
            indicatorView.setBackgroundResource(R.drawable.ic_reader_catalog_item_bg);
        }else if(tag.equals(CatalogView.TAG_DIGEST)){
            indicatorView = newIndicator(R.string.btn_text_bookdigest);
            indicatorView.setBackgroundResource(R.drawable.ic_reader_catalog_item_bg);
        }else if(tag.equals(CatalogView.TAG_BOOKMARK)){
            indicatorView = newIndicator(R.string.btn_text_bookmark);
        }
        return indicatorView;
    }

    public class ViewHolder {
        public ListView mListView;
        public ImageView mListViewBG;
        public View mLoadingView;
    }

    protected View newIndicator(int strResID) {
        TextView tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.tab_item_with_icon, null);
        tv.setId(android.R.id.title);
        tv.setText(strResID);
        return tv;
    };

    @Override
    public String getTab(int position) {
        return mTags.get(position);
    }

    @Override
    public int getCount() {
        return mTags.size();
    }

    @Override
    public View getItemView(ViewGroup container, int position) {
        String tag = getTab(position);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.reader_catalog_tab_item_lay, null);
        final ListView mListView = (ListView) contentView.findViewById(R.id.reader_catalog_lv);
        final ImageView mListViewBG = (ImageView) contentView.findViewById(R.id.reader_catalog_lv_bg);
        contentView.setTag(getItemViewTag(position));
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mListView = mListView;
        viewHolder.mListViewBG = mListViewBG;
        viewHolder.mLoadingView = contentView.findViewById(R.id.reader_catalog_loading_lay);
        contentView.setTag(R.layout.reader_catalog_tab_item_lay, viewHolder);
        mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if(view == null){
                    return;
                }
                view.destroyDrawingCache();
            }
        });
        AdapterView.OnItemClickListener onItemClickListener = null;
        if(tag.equals(CatalogView.TAG_CATALOG)){
            mListView.setAdapter(getCatalogAdapter());
            onItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                   onItemClicked(mListView, position);
                }
            };
        }
        mListView.setOnItemClickListener(onItemClickListener);
        return contentView;
    }

    public abstract ListAdapter getCatalogAdapter();


    public abstract void onItemClicked(ListView mListView, int position);

    public String getItemViewTag(int position){
        return ITEM_VIEW_TAG + "_" + position;
    }
}
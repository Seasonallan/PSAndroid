package com.season.example;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.quincysx.crypto.CoinTypes;
import com.season.btc.R;
import com.season.lib.BaseContext;
import com.season.mvp.ui.PageTurningActivity;

import java.util.ArrayList;
import java.util.List;

@Route(path= "/wallet/main")
public class WalletActivity extends PageTurningActivity {

    RecyclerView mRv;
    ChainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            getWindow().setAttributes(lp);
        }

        BaseContext.init(getApplicationContext());

        mRv = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRv.setLayoutManager(layoutManager);

        List<CoinTypes> list = new ArrayList<>();
        list.add(CoinTypes.Bitcoin);
        list.add(CoinTypes.Ethereum);
        list.add(CoinTypes.TRX);
        mAdapter = new ChainAdapter(this, list) {
            @Override
            public void onItemClick(int position) {
                super.onItemClick(position);
                BlockchainActivity.open(WalletActivity.this, mAdapter.mData.get(position));
            }
        };
        mRv.setAdapter(mAdapter);

    }

    @Override
    protected boolean isTopTileEnable() {
        return false;
    }

    @Override
    protected boolean isFullScreen() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_btc;
    }


    public class ChainAdapter extends RecyclerView.Adapter<ChainAdapter.ItemViewHolder> {
        Context mContext;

        private List<CoinTypes> mData;

        public ChainAdapter(Context context, List<CoinTypes> data) {
            mContext = context;
            mData = data;
        }


        public void onItemClick(int position) {
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_btc, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, final int position) {

            switch (mData.get(position)) {
                case Bitcoin:
                    holder.mIvPic.setImageResource(R.drawable.ic_circle_btc);
                    break;
                case Ethereum:
                    holder.mIvPic.setImageResource(R.drawable.ic_circle_eth);
                    break;
                case TRX:
                    holder.mIvPic.setImageResource(R.drawable.ic_circle_trx);
                    break;
                default:
                    break;
            }
            holder.mTvContent.setText(mData.get(position).coinName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView mIvPic;
            TextView mTvContent;

            public ItemViewHolder(View itemView) {
                super(itemView);
                mIvPic = itemView.findViewById(R.id.item_icon);
                mTvContent = itemView.findViewById(R.id.item_content);
            }

        }
    }
}
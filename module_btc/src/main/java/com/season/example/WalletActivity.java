package com.season.example;

import android.content.Context;
import android.content.Intent;
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
import com.quincysx.crypto.bip44.CoinEnum;
import com.season.btc.R;
import com.season.example.token.TokenActivity;
import com.season.lib.BaseContext;
import com.season.mvp.ui.PageTurningActivity;

import java.util.ArrayList;
import java.util.List;

@Route(path = "/wallet/main")
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

        List<CoinEnum> list = new ArrayList<>();
        list.add(CoinEnum.Bitcoin);
        list.add(CoinEnum.Ethereum);
        list.add(CoinEnum.TRX);
        list.add(CoinEnum.XRP);
        list.add(CoinEnum.EOS);
        list.add(CoinEnum.Litecoin);
        list.add(CoinEnum.Dogecoin);
        list.add(CoinEnum.BCH);
        list.add(CoinEnum.FIL);
        list.add(CoinEnum.TOKEN);

        mAdapter = new ChainAdapter(this, list) {
            @Override
            public void onItemClick(int position) {
                super.onItemClick(position);
                if (position >= getItemCount() - 1) {
                    startActivity(new Intent(WalletActivity.this, TokenActivity.class));
                    return;
                }
                BlockchainActivity.open(WalletActivity.this, mAdapter.mData.get(position));
            }
        };
        mRv.setAdapter(mAdapter);

        findViewById(R.id.k_chart).setOnClickListener(v -> {
            startActivity(new Intent(WalletActivity.this, SelectActivity.class));
        });
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

        private List<CoinEnum> mData;

        public ChainAdapter(Context context, List<CoinEnum> data) {
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
            holder.mIvPic.setImageResource(mData.get(position).coinIcon());
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
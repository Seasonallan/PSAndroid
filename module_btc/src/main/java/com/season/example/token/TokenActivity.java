package com.season.example.token;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.season.btc.R;
import com.season.mvp.ui.BaseTLEActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenActivity extends BaseTLEActivity {

    RecyclerView mRv;
    TokenAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_token);

        getTitleBar().enableLeftButton();
        getTitleBar().setTopTile("合约列表");


        mRv = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRv.setLayoutManager(layoutManager);


        findViewById(R.id.item_shib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TokenTransactionActivity.open(TokenActivity.this, Token.SHIB());
            }
        });

        mAdapter = new TokenAdapter(this, getTokens(TokenActivity.this)) {
            @Override
            public void onItemClick(int position) {
                super.onItemClick(position);
                TokenTransactionActivity.open(TokenActivity.this, mAdapter.mData.get(position));
            }
        };
        mRv.setAdapter(mAdapter);

    }

    public static List<Token> getTokens(Context context) {
        List<Token> tokenList = new ArrayList<>();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.token);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            String tokenStr = sb.toString();
            JSONObject jsonObject = new JSONObject(tokenStr);
            JSONArray array = jsonObject.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i).getJSONObject("tokenInfo");
                Token token = new Token();
                token.name = item.getString("s");
                token.desc = item.getString("f");
                token.address = item.getString("h");
                token.amount = array.getJSONObject(i).getString("balance");
                tokenList.add(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(tokenList);
        return tokenList;
    }


    public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.ItemViewHolder> {
        Context mContext;

        private List<Token> mData;

        public TokenAdapter(Context context, List<Token> data) {
            mContext = context;
            mData = data;
        }


        public void onItemClick(int position) {
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_token, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, final int position) {
            holder.mTvName.setText(mData.get(position).name);
            holder.mTvDesc.setText(mData.get(position).desc);
            holder.mTvAddress.setText(mData.get(position).address);
            holder.mTvAmount.setText(mData.get(position).amount);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView mTvName, mTvDesc, mTvAddress, mTvAmount;

            public ItemViewHolder(View itemView) {
                super(itemView);
                mTvName = itemView.findViewById(R.id.item_name);
                mTvDesc = itemView.findViewById(R.id.item_desc);
                mTvAddress = itemView.findViewById(R.id.item_address);
                mTvAmount = itemView.findViewById(R.id.item_amount);
            }

        }
    }
}
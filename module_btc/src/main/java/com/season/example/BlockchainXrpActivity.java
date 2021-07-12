package com.season.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.quincysx.crypto.bip44.CoinEnum;
import com.ripple.AitdOpenApi;
import com.ripple.bean.XRPAccount;
import com.season.btc.BtcOpenApi;
import com.season.btc.R;
import com.season.lib.util.LogUtil;
import com.season.mvp.ui.BaseTLEActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BlockchainXrpActivity extends BaseTLEActivity {


    public static void open(Context context, CoinEnum bookInfo) {
        Intent intent = new Intent();
        intent.setClass(context, BlockchainXrpActivity.class);
        intent.putExtra("coin", bookInfo.coinType());
        context.startActivity(intent);
    }

    CoinEnum coin;
    List<String> mWords;
    String seed;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AitdOpenApi.initSDK("https://s1.ripple.com:51234");
        AitdOpenApi.switchCoinModeXRP();

        handler = new Handler();
        coin = CoinEnum.parseCoinType(getIntent().getIntExtra("coin", CoinEnum.Bitcoin.coinType()));
        setContentView(R.layout.activity_chain);
        getTitleBar().setTopTile(coin.coinName());
        getTitleBar().enableLeftButton();

        mWords = BtcOpenApi.Wallet.createRandomMnemonic();

        seed = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWords = BtcOpenApi.Wallet.createRandomMnemonic();
                fillTime();
                fillContent(Arrays.toString(mWords.toArray()));
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seed = AitdOpenApi.Wallet.createFromMnemonic(mWords);
                fillTime();
                fillContent("--私钥: " + seed);
                fillContent("--地址: " + AitdOpenApi.Wallet.getAddress(seed));
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String accountInfo = AitdOpenApi.Request.getAccountInfo(AitdOpenApi.Wallet.getAddress(seed));
                        fillTime();
                        try {
                            fillContent(new JSONObject(accountInfo).toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.btn4).setVisibility(View.GONE);

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String hex = AitdOpenApi.Request.sign(seed,
                                "rNqKQoZzmYEXSafD2JU6pgNEp1BpJUU9oV", "0.5", "5", "000000");
                        fillTime();
                        fillContent(hex);
                    }
                }).start();
            }
        });



        log = findViewById(R.id.tv);
        scrollView = findViewById(R.id.c);
        log.setText("------------当前配置------------" + "\n" +
                "--Purpose: " + 44 + "\n" +
                "--Coin: " + coin.coinType() + "\n" +
                "--Account: " + 0 + "\n" +
                "--测试地址: " + getAddress() + "\n" +
                "");

    }

    private String getAddress(){
        //return ecKeyPair.getAddress();
        switch (coin) {
            case Bitcoin:
                return "183hmJGRuTEi2YDCWy5iozY8rZtFwVgahM";
            case Ethereum:
                return "0x9af168dcab9184561fdd9065812ec89d83e08d99";
            case XRP:
                return "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh";
            case TRX:
            default:
                return "TM2Hh95KyfUvwurTKBD2H5r84yh2QJdirG";
        }
    }


    JSONObject unspent;


    private void fillTime() {
        fillContent(new Date().toLocaleString() + ">>");
    }

    TextView log;
    ScrollView scrollView;

    /**
     * 进入文字
     *
     * @param response
     */
    public void fillContent(String response) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                log.setText(log.getText().toString()
                        + "\n"+ response);
                LogUtil.LOG(log.getText().toString());
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

}
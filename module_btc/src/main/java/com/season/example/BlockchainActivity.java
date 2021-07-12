package com.season.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.quincysx.crypto.CoinTypes;
import com.quincysx.crypto.ECKeyPair;
import com.quincysx.crypto.utils.HexUtils;
import com.season.btc.BtcOpenApi;
import com.season.btc.R;
import com.season.lib.support.http.DownloadAPI;
import com.season.lib.util.ToastUtil;
import com.season.mvp.ui.BaseTLEActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.tron.TronWalletApi;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BlockchainActivity extends BaseTLEActivity {


    public static void open(Context context, CoinTypes bookInfo) {
        Intent intent = new Intent();
        intent.setClass(context, BlockchainActivity.class);
        intent.putExtra("coin", bookInfo.coinType());
        context.startActivity(intent);
    }

    CoinTypes coin;
    List<String> mWords;
    ECKeyPair ecKeyPair;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        coin = CoinTypes.parseCoinType(getIntent().getIntExtra("coin", CoinTypes.Bitcoin.coinType()));
        setContentView(R.layout.activity_chain);
        getTitleBar().setTopTile(coin.coinName());
        getTitleBar().enableLeftButton();

        mWords = BtcOpenApi.Wallet.createRandomMnemonic();
        ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);

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
                ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
                fillTime();
                fillContent("--私钥: " + ecKeyPair.getPrivateKey());
                if (coin == CoinTypes.TRX){
                    org.tron.wallet.crypto.ECKey ecKey = org.tron.wallet.crypto.ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                    fillContent("-->地址: " + TronWalletApi.getAddress(ecKey.getPubKeyPoint()));
                }else{
                    fillContent("--地址: " + ecKeyPair.getAddress());
                }
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fillTime();
                    DownloadAPI.getRequestThread("https://services.tokenview.com/vipapi/addr/b/"+coin.coinName()+"/" +
                            getAddress() +
                            "?apikey=AnqHS6Rs2WX0hwFXlrv", new DownloadAPI.IHttpRequestListener() {
                        @Override
                        public void onCompleted(String result) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                fillContent(jsonObject.toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError() {
                            fillContent("error");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (coin.coinType() == CoinTypes.Bitcoin.coinType()){
            findViewById(R.id.btn4).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.btn4).setVisibility(View.GONE);
        }
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fillTime();
                    DownloadAPI.getRequestThread("https://services.tokenview.com/vipapi/unspent/btc/" +
                            getAddress() +
                            "/1/2?apikey=AnqHS6Rs2WX0hwFXlrv", new DownloadAPI.IHttpRequestListener() {
                        @Override
                        public void onCompleted(String result) {
                            try {
                                unspent = new JSONObject(result);
                                fillContent(unspent.toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError() {
                            fillContent("error");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (coin) {
                    case Bitcoin:
                        if (unspent == null){
                            ToastUtil.showToast("先获取未花费");
                            return;
                        }
                        String hex = BtcOpenApi.Wallet.signBtc(unspent, ecKeyPair,
                                "1DfDKUMzSxJD8dontsxTvXVUUBZrQ24ZfA", 0.5, 0.00001);
                        fillTime();
                        fillContent(hex);
                        break;
                    case Ethereum:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String hex = BtcOpenApi.Wallet.signEth(false, ecKeyPair,
                                        "1DfDKUMzSxJD8dontsxTvXVUUBZrQ24ZfA", new BigInteger("1000000000"), new BigInteger("300000")
                                        , new BigInteger("1"), "");
                                fillTime();
                                fillContent(hex);
                            }
                        }).start();
                        break;
                    case TRX:
                        break;
                }
            }
        });



        log = findViewById(R.id.tv);
        scrollView = findViewById(R.id.c);
        log.setText("------------当前配置------------" + "\n" +
                "--链ID: " + coin.coinName() + "\n" +
                "--RPC: " + coin.coinName() + "\n" +
                "--钱包地址: " + coin.coinName() + "\n" +
                "--钱包私钥: " + coin.coinName() + "\n" +
                "");

    }

    private String getAddress(){
        //return ecKeyPair.getAddress();
        switch (coin) {
            case Bitcoin:
                return "183hmJGRuTEi2YDCWy5iozY8rZtFwVgahM";
            case Ethereum:
                return "0x9af168dcab9184561fdd9065812ec89d83e08d99";
            case TRX:
            default:
                return "183hmJGRuTEi2Y2DCWy5iozY8rZtFwVgahM";
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
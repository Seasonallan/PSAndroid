package com.season.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.quincysx.crypto.ECKeyPair;
import com.quincysx.crypto.bip44.CoinEnum;
import com.quincysx.crypto.utils.HexUtils;
import com.season.btc.BtcOpenApi;
import com.season.btc.R;
import com.season.lib.support.http.DownloadAPI;
import com.season.lib.util.LogUtil;
import com.season.lib.util.ToastUtil;
import com.season.mvp.ui.BaseTLEActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.tron.TronWalletApi;
import org.tron.wallet.crypto.ECKey;
import org.tron.wallet.util.ByteArray;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.eblock.eos4j.OfflineSign;
import io.eblock.eos4j.Rpc;
import io.eblock.eos4j.api.vo.SignParam;

public class BlockchainActivity extends BaseTLEActivity {


    public static void open(Context context, CoinEnum bookInfo) {
        Intent intent = new Intent();
        intent.setClass(context, bookInfo == CoinEnum.XRP ? BlockchainXrpActivity.class : BlockchainActivity.class);
        intent.putExtra("coin", bookInfo.coinType());
        context.startActivity(intent);
    }

    CoinEnum coin;
    List<String> mWords;
    ECKeyPair ecKeyPair;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        coin = CoinEnum.parseCoinType(getIntent().getIntExtra("coin", CoinEnum.Bitcoin.coinType()));
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
                fillContent(Arrays.toString(mWords.toArray()).replaceAll(",", ""));
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
                fillTime();
                fillContent("--私钥: " + ecKeyPair.getPrivateKey());
                if (coin == CoinEnum.TRX) {
                    org.tron.wallet.crypto.ECKey ecKey = org.tron.wallet.crypto.ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                    fillContent("--地址: " + TronWalletApi.getAddress(ecKey.getPubKeyPoint()));
                } else {
                    fillContent("--地址: " + ecKeyPair.getAddress());
                }
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fillTime();
                    DownloadAPI.getRequestThread("https://services.tokenview.com/vipapi/addr/b/" + coin.coinName() + "/" +
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

        if (coin == CoinEnum.Bitcoin || coin == CoinEnum.Litecoin) {
            findViewById(R.id.btn4).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btn4).setVisibility(View.GONE);
        }
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fillTime();
                    DownloadAPI.getRequestThread("https://services.tokenview.com/vipapi/unspent/"+coin.coinName()+"/" +
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
                    case Litecoin:
                        if (unspent == null) {
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
                        String txId = "77ddfa7093cc5f745c0d3a54abb89ef070f983343c05e0f89e5a52f3e5401299";//先调用接口生成交易id
                        ECKey ecKey = ECKey.fromPrivate(ByteArray.fromHexString(ecKeyPair.getPrivateKey()));
                        String sign_hex = ecKey.sign(HexUtils.fromHex(txId)).toHex();
                        fillTime();
                        fillContent(sign_hex);
                        break;
                    case EOS:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Rpc rpc = new Rpc("https://eos.greymass.com:443");
                                // 获取离线签名参数
                                SignParam params = rpc.getOfflineSignParams(60l);
                                // 离线签名
                                OfflineSign sign = new OfflineSign();
                                try {
                                    String content = sign.sign(params, "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3", "eosio.token",
                                            "eeeeeeeeeeee", "555555555551", "372.0993 EOS", "test");
                                    fillTime();
                                    fillContent(new JSONObject(content).toString(4));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                }
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

    private String getAddress() {
        //return ecKeyPair.getAddress();
        switch (coin) {
            case Bitcoin:
                return "183hmJGRuTEi2YDCWy5iozY8rZtFwVgahM";
            case Litecoin:
                return "LajyQBeZaBA1NkZDeY8YT5RYYVRkXMvb2T";
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
                        + "\n" + response);
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
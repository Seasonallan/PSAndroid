package com.season.example.token;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.quincysx.crypto.BtcOpenApi;
import com.quincysx.crypto.ECKeyPair;
import com.quincysx.crypto.bip44.CoinEnum;
import com.quincysx.crypto.utils.HexUtils;
import com.season.btc.R;
import com.season.example.BlockchainFilActivity;
import com.season.example.BlockchainXrpActivity;
import com.season.example.Key;
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

public class TokenTransactionActivity extends BaseTLEActivity {


    public static void open(Context context, Token token) {
        Intent intent = new Intent();
        intent.setClass(context, TokenTransactionActivity.class);
        intent.putExtra("token", token);
        context.startActivity(intent);
    }

    Token token;
    List<String> mWords;
    ECKeyPair ecKeyPair;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        token = (Token) getIntent().getSerializableExtra("token");
        setContentView(R.layout.activity_chain);
        getTitleBar().setTopTile(token.name);
        getTitleBar().enableLeftButton();
 
        mWords = Arrays.asList(Key.sMnemonic.split(" "));
        fillContent(Arrays.toString(mWords.toArray()).replaceAll(",", ""));
        ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, CoinEnum.Ethereum);
        fillContent("--私钥: " + ecKeyPair.getPrivateKey());
        fillContent("--地址: " + ecKeyPair.getAddress());

        findViewById(R.id.btn1).setVisibility(View.GONE);

        findViewById(R.id.btn2).setVisibility(View.GONE);

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fillTime();
                    String address = "https://services.tokenview.com/vipapi/eth/address/tokenbalance/0x" +
                            ecKeyPair.getAddress().toLowerCase() +
                            "?apikey=" + Key.apiKey;
                    DownloadAPI.getRequestThread(address, new DownloadAPI.IHttpRequestListener() {
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
        findViewById(R.id.btn4).setVisibility(View.GONE);

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String hex = BtcOpenApi.Wallet.sighContract(true, ecKeyPair,
                                ecKeyPair.getAddress(), new BigInteger("23000000000"), new BigInteger("60000")
                                , "10000000000000000", token.address);
                        fillTime();
                        fillContent(hex);
                    }
                }).start();
            }
        });


        log = findViewById(R.id.tv);
        scrollView = findViewById(R.id.c);
        log.setText("------------当前配置------------" + "\n" +
                "--Name: " + token.name + "\n" +
                "--Description: " + token.desc + "\n" +
                "--Address: " + token.address + "\n" +
                "--测试地址: " + getAddress() + "\n" +
                "");

    }

    private String getAddress() {
        return "0x409b6b58fd454400370873c50985d242d6490ed5";
    }



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
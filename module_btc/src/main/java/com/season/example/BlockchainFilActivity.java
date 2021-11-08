package com.season.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.filecoinj.crypto.ECKey;
import com.filecoinj.handler.TransactionHandler;
import com.filecoinj.model.FileTransaction;
import com.filecoinj.utils.Base32;
import com.quincysx.crypto.BtcOpenApi;
import com.quincysx.crypto.bip39.SeedCalculator;
import com.quincysx.crypto.bip44.CoinEnum;
import com.quincysx.crypto.utils.Base64;
import com.quincysx.crypto.utils.HexUtils;
import com.season.btc.R;
import com.season.lib.support.http.DownloadAPI;
import com.season.lib.util.LogUtil;
import com.season.mvp.ui.BaseTLEActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ove.crypto.digest.Blake2b;

public class BlockchainFilActivity extends BaseTLEActivity {


    public static void open(Context context, CoinEnum bookInfo) {
        Intent intent = new Intent();
        intent.setClass(context, BlockchainFilActivity.class);
        intent.putExtra("coin", bookInfo.coinType());
        context.startActivity(intent);
    }

    CoinEnum coin;
    List<String> mWords;
    String seed;
    Handler handler;


    private String byteToAddress(byte[] pub) {
        Blake2b.Digest digest = Blake2b.Digest.newInstance(20);
        String hash = HexUtils.toHex(digest.digest(pub));

        //4.计算校验和
        String pubKeyHash = "01" + HexUtils.toHex(digest.digest(pub));

        Blake2b.Digest blake2b3 = Blake2b.Digest.newInstance(4);
        String checksum = HexUtils.toHex(blake2b3.digest(HexUtils.fromHex(pubKeyHash)));
        //5.生成地址

        return "f1" + Base32.encode(HexUtils.fromHex(hash + checksum)).toLowerCase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        coin = CoinEnum.parseCoinType(getIntent().getIntExtra("coin", CoinEnum.Bitcoin.coinType()));
        setContentView(R.layout.activity_chain);
        getTitleBar().setTopTile(coin.coinName());
        getTitleBar().enableLeftButton();

        mWords = BtcOpenApi.Wallet.createRandomMnemonic();
        mWords = Arrays.asList(Key.sMnemonic.split(" "));

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
                fillTime();

                byte[] seed = new SeedCalculator().calculateSeed(mWords, "");
                ECKey ecKey = ECKey.fromPrivate(seed);
                byte[] privateKeyBytes = ecKey.getPrivKeyBytes();
                byte[] pubKey = ecKey.getPubKey();
                String filAddress = byteToAddress(pubKey);
                String privateKey = HexUtils.toHex(privateKeyBytes);

                fillContent("--私钥: " + privateKey);
                fillContent("--地址: " + filAddress);
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fillTime();
                    DownloadAPI.getRequestThread("https://services.tokenview.com/vipapi/addr/b/" + coin.coinName() + "/" +
                            getAddress() +
                            "?apikey=" + Key.apiKey, new DownloadAPI.IHttpRequestListener() {
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

                byte[] seed = new SeedCalculator().calculateSeed(mWords, "");
                ECKey ecKeySeed = ECKey.fromPrivate(seed);
                ECKey ecKey = ECKey.fromPrivate(ecKeySeed.getPrivKeyBytes());
                byte[] pubKey = ecKey.getPubKey();
                String filAddress = byteToAddress(pubKey);

                try {
                    FileTransaction fileTransaction = new FileTransaction();
                    fileTransaction.from = filAddress;
                    fileTransaction.to = getAddress();
                    fileTransaction.nonce = 22L;
                    fileTransaction.method = 0L;
                    fileTransaction.params = "";
                    fileTransaction.gasPremium = "100053";
                    fileTransaction.gasLimit = 1078170L;
                    fileTransaction.gasFeeCap = "7580780488";
                    fileTransaction.value = "100000000000";

                    TransactionHandler transactionHandler = new TransactionHandler();
                    byte[] cidHash = transactionHandler.transactionSerialize(fileTransaction);
                    fillContent("--cidHash: ");
                    String data = Base64.encode(ecKey.sign(cidHash).toByteArray());
                    fillContent("--data: " + data);
                    //等待广播
                } catch (Exception e) {
                    e.printStackTrace();
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
        return "f1abjxfbp274xpdqcpuaykwkfb43omjotacm2p3za";
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
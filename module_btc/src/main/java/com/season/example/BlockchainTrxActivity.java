package com.season.example;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.quincysx.crypto.BtcOpenApi;
import com.quincysx.crypto.ECKeyPair;
import com.quincysx.crypto.bip44.CoinEnum;
import com.quincysx.crypto.utils.Base58Check;
import com.quincysx.crypto.utils.HexUtils;
import com.ripple.LogRipple;
import com.ripple.SimpleRequest;
import com.season.btc.R;
import com.season.lib.support.http.DownloadAPI;
import com.season.lib.util.LogUtil;
import com.season.mvp.ui.BaseTLEActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tron.TronWalletApi;
import org.tron.wallet.crypto.ECKey;
import org.tron.wallet.util.ByteArray;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BlockchainTrxActivity extends BaseTLEActivity {

    CoinEnum coin;
    List<String> mWords;
    ECKeyPair ecKeyPair;
    ECKey ecKey;
    Handler handler;
    double price = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        coin = CoinEnum.parseCoinType(getIntent().getIntExtra("coin", CoinEnum.Bitcoin.coinType()));
        setContentView(R.layout.activity_chain);
        getTitleBar().setTopTile(coin.coinName());
        getTitleBar().enableLeftButton();

        mWords = BtcOpenApi.Wallet.createRandomMnemonic();

        fillContent(Arrays.toString(mWords.toArray()).replaceAll(",", ""));
        ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
        fillContent("--私钥: " + ecKeyPair.getPrivateKey());
        ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
        fillContent("--地址: " + TronWalletApi.getAddress(ecKey.getPubKeyPoint()));




        String url = "https://services.tokenview.com/vipapi/coin/marketInfo/" + coin.coinName().toLowerCase() + "?apikey=" + Key.apiKey;
        LogUtil.LOG(url);
        DownloadAPI.getRequestThread(url, new DownloadAPI.IHttpRequestListener() {
            @Override
            public void onCompleted(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    price = jsonObject.getJSONObject("data").getDouble("priceUsd");
                    fillContent("当前价格：" + price);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError() {
                fillContent("error");
            }
        });

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
                ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                fillContent("--地址: " + TronWalletApi.getAddress(ecKey.getPubKeyPoint()));
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject js_request = new JSONObject();
                        try {
                            js_request.put("address", getAddress());
                            js_request.put("visible", true);
                            String res = SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/getaccount",
                                    js_request.toString());
                            JSONObject jsonObject = new JSONObject(res);
                            JSONObject showJson = new JSONObject();
                            BigInteger frozenBandWidth = new BigInteger("0");
                            if (jsonObject.has("frozen")) {
                                JSONArray array = jsonObject.getJSONArray("frozen");
                                for (int i = 0; i < array.length(); i++) {
                                    BigInteger frozenBandWidthItem = new BigInteger(array.getJSONObject(i).getString("frozen_balance"));
                                    frozenBandWidth = frozenBandWidth.add(frozenBandWidthItem);
                                }
                            }
                            showJson.put("可用余额", jsonObject.getString("balance"));
                            showJson.put("冻结宽带", frozenBandWidth.toString());
                            String frozenEnergy = jsonObject.getJSONObject("account_resource").getJSONObject("frozen_balance_for_energy").getString("frozen_balance");
                            showJson.put("冻结能量", frozenEnergy);

                            voteCount = frozenBandWidth.add(new BigInteger(frozenEnergy)).divide(new BigInteger("1000000")).intValue();
                            showJson.put("票数", voteCount);
                            fillContent(showJson.toString(4));

                            fillContent("价格：" + voteCount * price * 6.4);
                        } catch (JSONException e) {
                            LogRipple.error("exception", e);
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.btn4).setVisibility(View.GONE);
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        findViewById(R.id.btneX).setVisibility(View.VISIBLE);
        findViewById(R.id.btneX1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String res = freezeBalance(2000, "ENERGY"); //BANDWIDTH 或者 ENERGY
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));

                            String wordTest = Key.sMnemonic;
                            mWords = Arrays.asList(wordTest.split(" "));
                            ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
                            ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                            String sign_hex = ecKey.sign(rawData).toHex();

                            String hex = broadcastTransaction(jsonObject,
                                    sign_hex);

                            fillContent(new JSONObject(hex).toString(4));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });
        findViewById(R.id.btneX2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String res = unFreezeBalance();
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                            String wordTest = Key.sMnemonic;
                            mWords = Arrays.asList(wordTest.split(" "));
                            ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
                            ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                            String sign_hex = ecKey.sign(rawData).toHex();

                            String hex = broadcastTransaction(jsonObject,
                                    sign_hex);

                            fillContent(new JSONObject(hex).toString(4));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });


        findViewById(R.id.btneY).setVisibility(View.VISIBLE);
        findViewById(R.id.btneY1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<witnesses> witnessesList = witnesses();
                        try {
                            Collections.sort(witnessesList, new Comparator<witnesses>() {
                                @Override
                                public int compare(witnesses o1, witnesses o2) {
                                    return o1.voteCount - o2.voteCount < 0 ? 1 : -1;
                                }
                            });
                            witnessesList = witnessesList.subList(0, 5);
                            StringBuffer stringBuffer = new StringBuffer();
                            for (int i = 0; i < witnessesList.size(); i++) {
                                String address = witnessesList.get(i).address;
                                String brokerage = getBrokerage(address);
                                JSONObject jsonObject = new JSONObject(brokerage);
                                int brokerages = jsonObject.getInt("brokerage");
                                stringBuffer.append((address.substring(0, 4) + "***" + address.substring(address.length() - 4))
                                        + "（" + witnessesList.get(i).voteCount + "）"
                                        + ">>" + brokerages);
                                stringBuffer.append("\n");
                            }
                            fillContent(stringBuffer.toString());

                            String res = voteWitnessAccount(witnessesList.get(0).address, voteCount);

                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                            String wordTest = Key.sMnemonic;
                            mWords = Arrays.asList(wordTest.split(" "));
                            ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
                            ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                            String sign_hex = ecKey.sign(rawData).toHex();

                            String hex = broadcastTransaction(jsonObject,
                                    sign_hex);

                            fillContent(hex);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });
        findViewById(R.id.btneY2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject js_request = new JSONObject();
                        try {
                            js_request.put("address", getAddress());
                            js_request.put("visible", true);
                            String res = SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/getReward",
                                    js_request.toString());
                            JSONObject jsonObject = new JSONObject(res);
                            int reward = jsonObject.getInt("reward");
                            if (reward > 0) {
                                fillContent("reward="+reward);
                                js_request.put("owner_address", getAddress());
                                js_request.put("visible", true);
                                res = SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/withdrawbalance",
                                        js_request.toString());
                                jsonObject = new JSONObject(res);
                                fillContent(jsonObject.toString(4));

                                byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                                String wordTest = Key.sMnemonic;
                                mWords = Arrays.asList(wordTest.split(" "));
                                ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(mWords, coin);
                                ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));
                                String sign_hex = ecKey.sign(rawData).toHex();

                                String hex = broadcastTransaction(jsonObject,
                                        sign_hex);

                                fillContent(hex);
                            }
                            fillContent(jsonObject.toString(4));
                        } catch (JSONException e) {
                            LogRipple.error("exception", e);
                            fillContent(e.toString());
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fillTime();
                        String res = createTransaction(TronWalletApi.getAddress(ecKey.getPubKeyPoint()), 10);
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                            String sign_hex = ecKey.sign(rawData).toHex();
                            fillContent(sign_hex);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    int voteCount = 0;


    public String voteWitnessAccount(String vote_address, int vote_count) {
        JSONObject js_request = new JSONObject();
        try {
            JSONArray votes = new JSONArray();
            JSONObject voteItem = new JSONObject();
            voteItem.put("vote_address", vote_address);
            voteItem.put("vote_count", vote_count);
            votes.put(voteItem);

            js_request.put("owner_address", getAddress());
            js_request.put("votes", votes);
            js_request.put("visible", true);
            return SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/votewitnessaccount",
                    js_request.toString());
        } catch (JSONException e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    class witnesses {
        String address;
        long voteCount;
    }

    public List<witnesses> witnesses() {
        List<witnesses> witnesses = new ArrayList<>();
        try {
            String res = SimpleRequest.getRequest("http://18.133.82.227:8090/wallet/listwitnesses?visible=true");
            JSONObject object = new JSONObject(res);
            JSONArray array = object.getJSONArray("witnesses");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                witnesses witnessesItem = new witnesses();
                witnessesItem.address = item.getString("address");
                witnessesItem.voteCount = item.getLong("voteCount");
                witnesses.add(witnessesItem);
            }
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return witnesses;
    }

    public String getBrokerage(String address) {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("address", address);
            return SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/getBrokerage",
                    js_request.toString());
        } catch (JSONException e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    public String freezeBalance(int freeze_amount, String resource) {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("frozen_duration", 3); //冻结天数, 目前固定为 3 天
            js_request.put("owner_address", getAddress());
            js_request.put("resource", resource);
            js_request.put("frozen_balance", freeze_amount * 1000000);
            js_request.put("visible", true);
            return SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/freezebalance",
                    js_request.toString());
        } catch (JSONException e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    public String unFreezeBalance() {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("owner_address", getAddress());
            js_request.put("resource", "ENERGY"); //BANDWIDTH 或者 ENERGY
            js_request.put("visible", true);
            return SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/unfreezebalance",
                    js_request.toString());
        } catch (JSONException e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    public String createTransaction(String address, int amount) {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("to_address", address);
            js_request.put("owner_address", getAddress());
            js_request.put("amount", amount * 1000000);
            js_request.put("visible", true);
            return SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/createtransaction",
                    js_request.toString());
        } catch (JSONException e) {
            LogRipple.error("exception", e);
        }
        return "";
    }

    public String broadcastTransaction(JSONObject jsonObject, String signature) {
        try {
            JSONArray signatures = new JSONArray();
            signatures.put(signature);
            jsonObject.put("signature", signatures);
            return SimpleRequest.postRequest("http://18.133.82.227:8090/wallet/broadcasttransaction",
                    jsonObject.toString());
        } catch (JSONException e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    private String getAddress() {
        return "TFF8F3EGgCdR4GwDPyEdFVkGkTXvM9qGyu";
    }


    private String getTrc20Params(String address, BigInteger amount) throws Exception {
        Function transfer = transfer(HexUtils.toHex(Base58Check.base58ToBytes(address))
                .replaceFirst("41", "0x"), amount);
        String encodedFunction = FunctionEncoder.encode(transfer).substring(10);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contract_address", "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t");
        jsonObject.put("owner_address", getAddress());
        jsonObject.put("function_selector", "transfer(address,uint256)");
        jsonObject.put("parameter", encodedFunction);
        jsonObject.put("call_value", 0);
        jsonObject.put("fee_limit", 5000000);
        jsonObject.put("visible", true);

        return jsonObject.toString();
    }

    private Function transfer(String to, BigInteger value) {
        return new Function(
                "transfer",
                Arrays.asList(new org.web3j.abi.datatypes.Address(to), new Uint256(value)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
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
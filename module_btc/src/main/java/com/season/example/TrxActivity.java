package com.season.example;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

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
import org.json.JSONObject;
import org.tron.TronWalletApi;
import org.tron.wallet.crypto.ECKey;
import org.tron.wallet.util.ByteArray;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TrxActivity extends BaseTLEActivity {

    CoinEnum coin;
    ECKeyPair ecKeyPair;
    ECKey ecKey;
    Handler handler;
    EditText editText;
    String hosts = "https://api.trongrid.io"; //http://18.133.82.227:8090

    String getShowAmount(String amount){
        StringBuffer buffer = new StringBuffer();
        int size = 3;
        for (int i = 0; i < amount.length(); i++) {
            buffer.append(amount.charAt(amount.length() - 1 - i));
            size --;
            if (size == 0 && i < amount.length() - 1){
                buffer.append(",");
                size = 3;
            }
        }

        return buffer.reverse().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        coin = CoinEnum.TRX;
        setContentView(R.layout.activity_trx);
        getTitleBar().setTopTile("Trx Wallet");

        editText = findViewById(R.id.et_count);
        ecKeyPair = BtcOpenApi.Wallet.createFromMnemonic(BtcOpenApi.Wallet.createRandomMnemonic(), coin);
        ecKey = ECKey.fromPrivate(HexUtils.fromHex(ecKeyPair.getPrivateKey()));

        //查询余额，已完成
        findViewById(R.id.btnAmount).setOnClickListener(v -> {
            fillTime();
            new Thread(() -> {
                JSONObject js_request = new JSONObject();
                try {
                    js_request.put("address", getAddress());
                    js_request.put("visible", true);
                    String res = SimpleRequest.postRequest(hosts + "/wallet/getaccount",
                            js_request.toString());
                    JSONObject jsonObject = new JSONObject(res);
                    JSONObject showJson = new JSONObject();
                    String balance = jsonObject.getString("balance");
                    showJson.put("可用余额", getShowAmount(balance));
                    BigInteger frozenBandWidth = new BigInteger("0");
                    if (jsonObject.has("frozenV2")) {
                        JSONArray array = jsonObject.getJSONArray("frozenV2");
                        for (int i = 0; i < array.length(); i++) {
                            if (array.getJSONObject(i).has("amount")){
                                BigInteger frozenBandWidthItem = new BigInteger(array.getJSONObject(i).getString("amount"));
                                frozenBandWidth = frozenBandWidth.add(frozenBandWidthItem);
                            }
                        }
                    }
                    showJson.put("冻结宽带", getShowAmount(frozenBandWidth.toString()));
                    if (jsonObject.has("unfrozenV2")) {
                        JSONArray array = jsonObject.getJSONArray("unfrozenV2");
                        for (int i = 0; i < array.length(); i++) {
                            showJson.put("解冻宽带-" + (i + 1), getShowAmount(array.getJSONObject(i).getString("unfreeze_amount"))
                                    + "，到期时间:"+ new Date(array.getJSONObject(i).getLong("unfreeze_expire_time")).toLocaleString());
                        }
                    }

                    voteCount = frozenBandWidth.divide(new BigInteger("1000000")).intValue();
                    showJson.put("票数", voteCount);
                    if (jsonObject.has("votes")){
                        JSONArray array = jsonObject.getJSONArray("votes");
                        for (int i = 0; i < array.length(); i++) {
                            showJson.put("投票-" + (i + 1), array.getJSONObject(i).getString("vote_address")
                                    + "，数量:"+ array.getJSONObject(i).getString("vote_count"));
                        }
                    }
                    fillContent(showJson.toString(4));

                } catch (Exception e) {
                    LogRipple.error("exception", e);
                }
            }).start();
        });

        //冻结，已完成
        findViewById(R.id.btnFroze).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String res = freezeBalance(Integer.parseInt(editText.getText().toString()), "BANDWIDTH"); //BANDWIDTH 或者 ENERGY
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));

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

        //解冻，已完成
        findViewById(R.id.btnUnFroze).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String res = unFreezeBalance(Integer.parseInt(editText.getText().toString()));
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
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

        //取消解冻，已完成
        findViewById(R.id.btnUnFrozeCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillTime();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String res = unFreezeCancel();
                        try {
                            JSONObject jsonObject = new JSONObject(res);
                            fillContent(jsonObject.toString(4));
                            byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
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

        //投票，已完成
        findViewById(R.id.btnVote).setOnClickListener(new View.OnClickListener() {
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

        //收益，已完成
        findViewById(R.id.btnProfit).setOnClickListener(new View.OnClickListener() {
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
                            String res = SimpleRequest.postRequest(hosts + "/wallet/getReward",
                                    js_request.toString());
                            JSONObject jsonObject = new JSONObject(res);
                            int reward = jsonObject.getInt("reward");
                            if (reward > 0) {
                                fillContent("reward="+reward);
                                js_request.put("owner_address", getAddress());
                                js_request.put("visible", true);
                                res = SimpleRequest.postRequest(hosts + "/wallet/withdrawbalance",
                                        js_request.toString());
                                jsonObject = new JSONObject(res);
                                fillContent(jsonObject.toString(4));

                                byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                                String sign_hex = ecKey.sign(rawData).toHex();

                                String hex = broadcastTransaction(jsonObject,
                                        sign_hex);

                                fillContent(hex);
                            }
                            fillContent(jsonObject.toString(4));
                        } catch (Exception e) {
                            LogRipple.error("exception", e);
                            fillContent(e.toString());
                        }
                    }
                }).start();
            }
        });

        //转账，已完成
        findViewById(R.id.btnSignExchange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 创建一个AlertDialog构造器
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TrxActivity.this);
                builder.setTitle("密码验证");
                View view = LayoutInflater.from(TrxActivity.this).inflate(R.layout.dialog_set, null);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取输入框的内容
                        String key = ((EditText)view.findViewById(R.id.et_name)).getText().toString();
                        if ("900615".equals(key)){

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    fillTime();
                                    String res = createTransaction("TVxTMHP2Gk2AKcMPmSuSLNStgCFTK4b9uf", Integer.parseInt(editText.getText().toString()));
                                    try {
                                        JSONObject jsonObject = new JSONObject(res);
                                        fillContent(jsonObject.toString(4));
                                        byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                                        String sign_hex = ecKey.sign(rawData).toHex();
                                        fillContent(sign_hex);

                                        String hex = broadcastTransaction(jsonObject,
                                                sign_hex);
                                        fillContent(hex);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }else{
                            fillContent("密码错误");
                        }
                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //转账，已完成
        findViewById(R.id.btnSignGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 创建一个AlertDialog构造器
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(TrxActivity.this);
                builder.setTitle("密码验证");
                View view = LayoutInflater.from(TrxActivity.this).inflate(R.layout.dialog_set, null);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取输入框的内容
                        String key = ((EditText)view.findViewById(R.id.et_name)).getText().toString();
                        if ("900615".equals(key)){

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    fillTime();
                                    String res = createTransaction("TA1kh61TQzQbNjLt3ruNcnWCj8awbQiS3a", Integer.parseInt(editText.getText().toString()));
                                    try {
                                        JSONObject jsonObject = new JSONObject(res);
                                        fillContent(jsonObject.toString(4));
                                        byte[] rawData = ByteArray.fromHexString(jsonObject.getString("txID"));
                                        String sign_hex = ecKey.sign(rawData).toHex();
                                        fillContent(sign_hex);

                                        String hex = broadcastTransaction(jsonObject,
                                                sign_hex);
                                        fillContent(hex);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }else{
                            fillContent("密码错误");
                        }
                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
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
            return SimpleRequest.postRequest(hosts + "/wallet/votewitnessaccount",
                    js_request.toString());
        } catch (Exception e) {
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
            String res = SimpleRequest.getRequest(hosts + "/wallet/listwitnesses?visible=true");
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
            return SimpleRequest.postRequest(hosts + "/wallet/getBrokerage",
                    js_request.toString());
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    public String freezeBalance(int freeze_amount, String resource) {
        JSONObject js_request = new JSONObject();
        try {
            //js_request.put("frozen_duration", 3); //冻结天数, 目前固定为 3 天
            js_request.put("owner_address", getAddress());
            js_request.put("resource", resource);
            js_request.put("frozen_balance", freeze_amount * 1000000l);
            js_request.put("visible", true);
            return SimpleRequest.postRequest(hosts + "/wallet/freezebalancev2",
                    js_request.toString());
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    public String unFreezeBalance(int freeze_amount) {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("owner_address", getAddress());
            js_request.put("resource", "BANDWIDTH"); //BANDWIDTH 或者 ENERGY
            js_request.put("unfreeze_balance", freeze_amount * 1000000l); //"unfreeze_balance": 1000000,
            js_request.put("visible", true); //"unfreeze_balance": 1000000,
            return SimpleRequest.postRequest(hosts + "/wallet/unfreezebalancev2",
                    js_request.toString());
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return "";
    }

    public String unFreezeCancel() {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("owner_address", getAddress());
            js_request.put("visible", true);
            return SimpleRequest.postRequest(hosts + "/wallet/cancelallunfreezev2",
                    js_request.toString());
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    public String createTransaction(String address, int amount) {
        JSONObject js_request = new JSONObject();
        try {
            js_request.put("to_address", address);
            js_request.put("owner_address", getAddress());
            js_request.put("amount", amount * 1000000l);
            js_request.put("visible", true);
            return SimpleRequest.postRequest(hosts + "/wallet/createtransaction",
                    js_request.toString());
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return "";
    }

    public String broadcastTransaction(JSONObject jsonObject, String signature) {
        try {
            JSONArray signatures = new JSONArray();
            signatures.put(signature);
            jsonObject.put("signature", signatures);
            return SimpleRequest.postRequest(hosts + "/wallet/broadcasttransaction",
                    jsonObject.toString());
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return "";
    }


    private String getAddress() {
        return TronWalletApi.getAddress(ecKey.getPubKeyPoint());
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
package io.eblock.eos4j.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.eblock.eos4j.api.vo.Block;
import io.eblock.eos4j.api.vo.ChainInfo;
import io.eblock.eos4j.api.vo.account.Account;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Generator {

    public static String baseUrl;

    public static void createService(String baseUrl) {
        Generator.baseUrl = baseUrl;
    }

    public static ChainInfo getChainInfo() throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chain/get_info")
                .get()//默认就是GET请求，可以不写
                .build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        String json1 = response.body().string();
        return new ObjectMapper().readValue(json1, ChainInfo.class);
    }

    public static Block getBlock(String block_num_or_id) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("block_num_or_id", block_num_or_id).build();
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chain/get_block")
                .post(formBody)
                .build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        String json1 = response.body().string();
        return new ObjectMapper().readValue(json1, Block.class);
    }

    public static Account getAccount(String account_name) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("account_name", account_name).build();
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chain/get_account")
                .post(formBody)
                .build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        String json1 = response.body().string();
        return new ObjectMapper().readValue(json1, Account.class);
    }


}

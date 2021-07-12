package com.ripple;

import com.ripple.bean.XRPAccount;
import com.ripple.bean.XRPFee;
import com.ripple.bean.XRPLedger;
import com.ripple.bean.XRPTransaction;
import com.ripple.bean.transaction.XRPAccount_TransactionList;
import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.signed.SignedTransaction;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.Seed;
import com.ripple.word.MnemonicGenerator;
import com.ripple.word.RandomSeed;
import com.ripple.word.SeedCalculator;
import com.ripple.word.WordCount;
import com.ripple.word.wordlists.English;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * aitd钱包相关API
 */
public class AitdOpenApi {

    public static String DOMAIN;

    /**
     * 设置请求域名
     *
     * @param domain
     */
    public static void initSDK(String domain) {
        DOMAIN = domain;
    }

    /**
     * 切换模式为 XRP模式
     */
    public static void switchCoinModeXRP() {
        Config.setAlphabet(Config.ALPHABET_XRP);
    }

    /**
     * 切换模式为 AITD模式
     */
    public static void switchCoinModeAITD() {
        Config.setAlphabet(Config.ALPHABET_AITD);
    }

    /**
     * 钱包相关
     */
    public static class Wallet {


        /**
         * 创建一个随机的钱包
         *
         * @return
         */
        public static String createRandomWallet() {
            return createFromMnemonic(createRandomMnemonic());
        }

        /**
         * 地址是否有效
         * @return
         */
        public static boolean isAddressValid(String address){
            try {
                AccountID.fromAddress(address);
                return true;
            }catch (Exception e){
                LogRipple.error("exception", e);
            }
            return false;
        }

        /**
         * 通过助记词创建钱包
         *
         * @param list
         * @return
         */
        public static String createFromMnemonic(List<String> list) {
            byte[] seed = new SeedCalculator().calculateSeed(list, "");  //助记词生成种子。
            byte[] seedRes = new byte[16];
            System.arraycopy(seed, 0, seedRes, 0, 16);
            Seed seedKey = Seed.fromSeedKey(seedRes);
            return seedKey.toString();
        }

        /**
         * 通过助记词创建钱包
         *
         * @param list
         * @return
         */
        public static Seed createSeedFromMnemonic(List<String> list) {
            byte[] seed = new SeedCalculator().calculateSeed(list, "");  //助记词生成种子。
            byte[] seedRes = new byte[16];
            System.arraycopy(seed, 0, seedRes, 0, 16);
            Seed seedKey = Seed.fromSeedKey(seedRes);
            return seedKey;
        }

        /**
         * 创建随机助记词
         *
         * @return
         */
        public static List<String> createRandomMnemonic() {
            byte[] random = RandomSeed.random(WordCount.TWELVE);
            return new MnemonicGenerator(English.INSTANCE).createMnemonic(random);
        }

        /**
         * 通过私钥获取地址
         *
         * @param seed
         * @return
         */
        public static String getAddress(String seed) {
            return AccountID.fromSeedString(seed).address;
        }


    }


    /**
     * 请求相关
     */
    public static class Request {

        /**
         * 获取AITD开户需要的金额
         *
         * @return
         */
        public static String getBaseAitd() {
            JSONArray emptyParams = new JSONArray();
            emptyParams.put(new JSONObject());
            String res = SimpleRequest.postRequest(DOMAIN, buildRequestBody("server_info", emptyParams));
            try {
                JSONObject responseObject = new JSONObject(res);
                return responseObject.getJSONObject("result").getJSONObject("info")
                                        .getJSONObject("validated_ledger").getString("reserve_base_aitd");
            } catch (Exception e) {
                LogRipple.error("exception", e);
            }
            return "20";
        }



        /**
         * 获取手续费
         *
         * @return
         */
        public static XRPFee getFee() {
            JSONArray emptyParams = new JSONArray();
            emptyParams.put(new JSONObject());
            String res = SimpleRequest.postRequest(DOMAIN, buildRequestBody("fee", emptyParams));
            XRPFee xrpFee = new XRPFee();
            try {
                JSONObject responseObject = new JSONObject(res);
                xrpFee.fromJsonObject(responseObject.getJSONObject("result"));
            } catch (Exception e) {
                LogRipple.error("exception", e);
            }
            return xrpFee;
        }


        /**
         * 获取账户余额
         *
         * @return
         */
        public static XRPAccount parseAccountInfo(String address) {
            String res = getAccountInfo(address);
            //LogRipple.printForce(address + "--:" + res);
            XRPAccount xrpAccount = new XRPAccount();
            try {
                JSONObject responseObject = new JSONObject(res);
                xrpAccount.fromJsonObject(responseObject.getJSONObject("result"));
            } catch (Exception e) {
                LogRipple.error("exception", e);
            }
            return xrpAccount;
        }


        /**
         * 获取账户余额
         *
         * @return
         */
        public static String getAccountInfo(String address) {
            JSONArray transactionParams = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("account", address);
            } catch (JSONException e) {
                LogRipple.error("exception", e);
            }

            transactionParams.put(jsonObject);
            return SimpleRequest.postRequest(DOMAIN, buildRequestBody("account_info", transactionParams));
        }

        /**
         * 获取交易列表
         *
         * @return
         */
        public static XRPAccount_TransactionList getTransactionList(String address, int limit, int ledger, int seq) {
            JSONArray transactionParams = new JSONArray();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("account", address);
                jsonObject.put("binary", false); //返回json格式
                jsonObject.put("limit", limit);

                if (ledger > 0) { //分页
                    JSONObject marker = new JSONObject();
                    marker.put("ledger", ledger);
                    marker.put("seq", seq);
                    jsonObject.put("marker", marker);
                }

            } catch (JSONException e) {
                LogRipple.error("exception", e);
            }
            transactionParams.put(jsonObject);
            String res = SimpleRequest.postRequest(DOMAIN, buildRequestBody("account_tx", transactionParams));
            XRPAccount_TransactionList xrpAccountTransactionList = new XRPAccount_TransactionList();
            try {
                JSONObject responseObject = new JSONObject(res);
                xrpAccountTransactionList.fromJsonObject(responseObject.getJSONObject("result"));
            } catch (Exception e) {
                LogRipple.error("exception", e);
            }
            return xrpAccountTransactionList;
        }


        /**
         * 转账
         *
         * @return
         */
        public static XRPTransaction transaction(String seed, String destination, String fee, String amount) {
            return transaction(seed, destination, fee, amount, "1");
        }

        /**
         * 转账
         *
         * @return
         */
        public static XRPTransaction transaction(String seed, String destination, String fee, String amount, String tag) {
            return transaction(sign(seed, destination, fee, amount, tag));
        }

        /**
         * 转账
         *
         * @return
         */
        public static String sign(String seed, String destination, String fee, String amount, String tag) {
            String address = Wallet.getAddress(seed);
            XRPAccount xrpAccount = parseAccountInfo(address);
            if (xrpAccount != null && xrpAccount.account_data != null){
                Payment payment = new Payment();
                payment.as(AccountID.Account, address);
                payment.as(AccountID.Destination, destination);
                payment.as(UInt32.DestinationTag, tag);
                payment.as(Amount.Amount, amount);
                payment.as(UInt32.Sequence, xrpAccount.account_data.Sequence);
                payment.as(UInt32.LastLedgerSequence, xrpAccount.ledger_current_index + 1);
                payment.as(Amount.Fee, fee);
                SignedTransaction signed = payment.sign(seed);
                String tx_blob = signed.tx_blob;
                return tx_blob;
            }else{
                return null;
            }
        }

        /**
         * 转账
         *
         * @return
         */
        public static XRPTransaction transaction(String tx_blob) {
            JSONArray transactionParams = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("tx_blob", tx_blob);
            } catch (JSONException e) {
                LogRipple.error("exception", e);
            }

            transactionParams.put(jsonObject);

            String res = SimpleRequest.postRequest(DOMAIN, buildRequestBody("submit", transactionParams));
            XRPTransaction xrpTransaction = new XRPTransaction();
            try {
                JSONObject responseObject = new JSONObject(res);
                xrpTransaction.fromJsonObject(responseObject.getJSONObject("result"));
            } catch (Exception e) {
                LogRipple.error("exception", e);
            }
            return xrpTransaction;
        }


        /**
         * 关闭账单
         *
         * @return
         */
        public static XRPLedger close() {
            JSONArray emptyParams = new JSONArray();
            emptyParams.put(new JSONObject());

            String res = SimpleRequest.postRequest(DOMAIN, buildRequestBody("ledger_accept", emptyParams));
            XRPLedger xrpLedger = new XRPLedger();
            try {
                JSONObject responseObject = new JSONObject(res);
                xrpLedger.fromJsonObject(responseObject.getJSONObject("result"));
            } catch (Exception e) {
                LogRipple.error("exception", e);
            }
            return xrpLedger;
        }


        private static String buildRequestBody(String method, JSONArray params) {
            JSONObject js_request = new JSONObject();
            try {
                js_request.put("method", method);
                js_request.put("params", params);
            } catch (JSONException e) {
                LogRipple.error("exception", e);
            }
            return js_request.toString();
        }

    }

}

package com.quincysx.crypto;


import com.quincysx.crypto.bip44.CoinEnum;
import com.quincysx.crypto.ECKeyPair;
import com.quincysx.crypto.bip32.ExtendedKey;
import com.quincysx.crypto.bip32.ValidationException;
import com.quincysx.crypto.bip39.MnemonicGenerator;
import com.quincysx.crypto.bip39.RandomSeed;
import com.quincysx.crypto.bip39.SeedCalculator;
import com.quincysx.crypto.bip39.WordCount;
import com.quincysx.crypto.bip39.wordlists.English;
import com.quincysx.crypto.bip44.AddressIndex;
import com.quincysx.crypto.bip44.BIP44;
import com.quincysx.crypto.bip44.CoinPairDerive;
import com.quincysx.crypto.bitcoin.BTCTransaction;
import com.quincysx.crypto.utils.HexUtils;
import com.season.lib.support.http.DownloadAPI;
import com.season.lib.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * btc钱包相关API
 */
public class BtcOpenApi {

    /**
     * 钱包相关
     */
    public static class Wallet {

        /**
         * 通过助记词创建钱包
         *
         * @param list
         * @return
         */
        public static ECKeyPair createFromMnemonic(List<String> list, CoinEnum coinEnum) {
            byte[] seed = new SeedCalculator().calculateSeed(list, "");
            ExtendedKey extendedKey = null;
            ECKeyPair master = null;
            try {
                extendedKey = ExtendedKey.create(seed);
                AddressIndex address = BIP44.m().purpose44()
                        .coinType(coinEnum)
                        .account(0)
                        .external()
                        .address(0);
                CoinPairDerive coinKeyPair = new CoinPairDerive(extendedKey);
                master = coinKeyPair.derive(address);
                return master;
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            return null;
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
         * BTC签名
         *
         * @param unspent
         * @param master
         * @param toAddress
         * @param amount
         * @param miner_fee_total
         * @return
         */
        public static String signBtc(JSONObject unspent, ECKeyPair master,
                                     String toAddress, double amount, double miner_fee_total) {
            try {
                double total_value = 0;//用于交易的未交易总余额
                //根据金额选择需要消耗的未花费txID;
                List<JSONObject> utxo_for_pay_list = new ArrayList<>();
                JSONArray data = unspent.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonObject = data.getJSONObject(i);
                    double value = jsonObject.getDouble("value");
                    total_value += value;
                    utxo_for_pay_list.add(jsonObject);
                    if (total_value + miner_fee_total >= amount) {
                        break;
                    }
                }

                BTCTransaction.Input[] inputs = new BTCTransaction.Input[utxo_for_pay_list.size()];
                for (int i = 0; i < utxo_for_pay_list.size(); i++) {
                    BTCTransaction.OutPoint outPoint = new BTCTransaction.OutPoint(HexUtils.fromHex(utxo_for_pay_list.get(i).getString("txid")),
                            utxo_for_pay_list.get(i).getInt("block_no"));
                    BTCTransaction.Script script002 = BTCTransaction.Script.buildOutput(master.getAddress());  //自己的地址
                    BTCTransaction.Input input = new BTCTransaction.Input(outPoint, script002, 0);
                    inputs[i] = input;
                }

                //如果剩余的金额大于550聪，就加入找零的输出，输出的地址是自己。
                BTCTransaction.Output[] outputs;
                double change_value = total_value - amount - miner_fee_total;
                if (change_value > 0.0000055) {//留有余量防止粉尘交易。
                    outputs = new BTCTransaction.Output[2];
                    BTCTransaction.Script script = BTCTransaction.Script.buildOutput(toAddress);  //对方的地址
                    long trans_value = (long) (Double.valueOf(amount) * 100000000l);
                    BTCTransaction.Output output = new BTCTransaction.Output(trans_value, script);
                    outputs[0] = output;
                    BTCTransaction.Script script_change = BTCTransaction.Script.buildOutput(master.getAddress());  //找零地址，即自己的地址
                    long change_value_l = (long) (change_value * 100000000l);
                    BTCTransaction.Output output_change = new BTCTransaction.Output(change_value_l, script_change);
                    outputs[1] = output_change;
                } else {
                    outputs = new BTCTransaction.Output[1];
                    BTCTransaction.Script script = BTCTransaction.Script.buildOutput(toAddress);  //对方的地址
                    long trans_value = (long) (Double.valueOf(amount) * 100000000l);
                    BTCTransaction.Output output = new BTCTransaction.Output(trans_value, script);
                    outputs[0] = output;
                }

                BTCTransaction unsignedTransaction = new BTCTransaction(inputs, outputs, 0);
                byte[] sign = unsignedTransaction.sign(master);   //签名。
                String toHex = HexUtils.toHex(sign);              //把签名转成hex
                return toHex;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * ETH转账签名
         *
         * @param broadCast
         * @param master
         * @param toAddress
         * @param gasPrice
         * @param gasLimit
         * @param amount
         * @param data
         * @return
         * @throws Exception
         */
        public static String signEth(boolean broadCast, ECKeyPair master,
                                     String toAddress, BigInteger gasPrice, BigInteger gasLimit,
                                     BigInteger amount, String data) {
            try {
                String rpc = "http://192.168.1.11:8545";
                long chainId = 1337;

                Web3j web3j = Web3j.build(new HttpService(rpc));

                EthGetTransactionCount c = web3j.ethGetTransactionCount("0x"+master.getAddress(), DefaultBlockParameterName.PENDING).send();


                RawTransaction rtx = RawTransaction.createTransaction(c.getTransactionCount(),
                        gasPrice, gasLimit, toAddress, amount, data);


                BigInteger key = new BigInteger(master.getPrivateKey(), 16);
                org.web3j.crypto.ECKeyPair keypair = org.web3j.crypto.ECKeyPair.create(key);

                Credentials credentials = Credentials.create(keypair);
                byte[] signedMessage = TransactionEncoder.signMessage(rtx, chainId, credentials);
                String hexValue = Numeric.toHexString(signedMessage);

                if (!broadCast) {
                    return hexValue;
                }
                EthSendTransaction raw = web3j
                        .ethSendRawTransaction(hexValue)
                        .send();
                if (!raw.hasError()) {
                    return raw.getTransactionHash();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }


        /**
         * 请求相关
         */
        public static class Request {


            /**
             * 获取账户余额
             *
             * @return
             */
            public static boolean getAccountInfo(String address) {
                //address = "bc1qjvkwdrkmvhx9d07lg4fclvh2l0nnsy5nv5vu7k";
                try {
                    String url = "https://tokenview.com/api/search/" + address;
                    // String url = "https://services.tokenview.com/vipapi" + "/address/btc/" + address + "/1%2F1?&apikey=AnqHS6Rs2WX0hwFXlrv";
                    //LogRipple.printForce(url);
                    String res = DownloadAPI.getRequest(url);
                    if (res != null) {
                        LogUtil.LOG(res);
                        JSONObject jsonObject = new JSONObject(res);
                        if (jsonObject.getInt("code") == 1) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                //LogRipple.printForce(address + "--:" + res);

                return false;
            }


        }

    }
}

package com.quincysx.crypto.bip44;


import com.season.btc.R;

/**
 * Created by q7728 on 2018/3/18.
 */

public enum CoinEnum {
    Bitcoin(0, "BTC", R.drawable.ic_circle_btc),
    BitcoinTest(1, "BTC", R.drawable.ic_circle_btc),
    Litecoin(2, "LTC", R.drawable.ic_circle_ltc),
    Dogecoin(3, "DOGE", R.drawable.ic_circle_doge),
    Ethereum(60, "ETH", R.drawable.ic_circle_eth),
    EOS(194, "EOS", R.drawable.ic_circle_eos),
    XRP(144, "XRP", R.drawable.ic_circle_xrp),
    BCH(145, "BCH", R.drawable.ic_circle_bch),
    TRX(195, "TRX", R.drawable.ic_circle_trx),
    FIL(-1, "FIL", R.drawable.ic_circle_fil),
    TOKEN(-2, "合约代币", R.drawable.ic_circle_shib);



    private int coinType;
    private String coinName;
    private int coinIcon;

    CoinEnum(int i, String name, int id) {
        coinType = i;
        coinName = name;
        coinIcon = id;
    }

    public int coinIcon() {
        return coinIcon;
    }

    public int coinType() {
        return coinType;
    }

    public String coinName() {
        return coinName;
    }

    public static CoinEnum parseCoinType(int type){
        for (CoinEnum e : CoinEnum.values()) {
            if (e.coinType == type) {
                return e;
            }
        }
        return CoinEnum.Bitcoin;
    }
}

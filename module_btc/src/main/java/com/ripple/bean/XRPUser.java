package com.ripple.bean;

import java.io.Serializable;

/**
 * "account_id":"rQHfAsZLU771GL79ocCCdMpwrEUQYB6Tst",
 * "key_type":"secp256k1",
 * "master_key":"SEES TEST LOW LIT SCOT TAN NINA GAGE EST BIN DEN SORT",
 * "master_seed":"snTUsizZoydtkmyBnUMFLVgN4RVNv",
 * "master_seed_hex":"C5DFD083470091C47E7C8D129834FDD8",
 * "public_key":"aBRfLvtivhYUaUfGgMhhwNuRTSRA3caADQ375c2fiZQg6gWsTEZP",
 * "public_key_hex":"039F2B31AB494A5E411E9CCA6FD9904C28E4B9893E9F71056BCFA592AE09C20C30",
 */
public class XRPUser extends BaseResult  implements Serializable {
    public String account_id;
    public String key_type;
    public String master_key;
    public String master_seed;
    public String master_seed_hex;
    public String public_key;
    public String public_key_hex;
}

package com.ripple.bean;

import com.ripple.LogRipple;
import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 * "Account": "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",
 * "Amount": "1000000000",
 * "Destination": "rDjW7L93g1rMbSEKTiJ7RV957bHQkBqrKt",
 * "DestinationTag": 1,
 * "Fee": "10",
 * "Flags": 2147483648,
 * "InvoiceID": "B4976E3BF8FFEDC0DFD82E974A40C46C3E5B542C714AA6A2FB4B611AB244E04E",
 * "Sequence": 4,
 * "SigningPubKey": "0330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD020",
 * "TransactionType": "Payment",
 * "TxnSignature": "3045022100A3059E444ADEA2A02B102CC5CA1A2146372B5C265B63D196676FC1B034119DC70220671A9EAB838B224DA6F1E737AD80C7A15666C88AA232C484F8E5A5B2B7B4A39B",
 * "hash": "2BF3330080E0EEFFF0ABB8FE568FE69703BB489CA7C735980A6CF39DDBE74701"
 * 交易列表
 * *                     "Account": "rD1Wjng7VakncxMtLCL9CT9EU8M8jXyYsv",
 * *                     "Amount": "1000",
 * *                     "Destination": "rNqKQoZzmYEXSafD2JU6pgNEp1BpJUU9oV",
 * *                     "DestinationTag": 1,
 * *                     "Fee": "100",
 * *                     "Flags": 2147483648,
 * *                     "LastLedgerSequence": 24,
 * *                     "Sequence": 7,
 * *                     "SigningPubKey": "03DC60EB76FD89DCB5CAFF4CC90F7CD635372A81C9D1FC83079995D77AF2740D1E",
 * *                     "TransactionType": "Payment",
 * *                     "TxnSignature": "30450221008F1EE23DBB30E121C620036D1FE865B8DDBAEF6D6431509125C96265C682439A0220108BDA757BCE5C6000702E59D1D41F4F7FD13F012DD1B18227BDE91E1B29667D",
 * <p>
 * *                     "date": 652529510,
 * *                     "hash": "4BB79AE337A4792F04A82C5E7F0372B7A91CA62581C87FDE2D7E5693A85B76CB",
 * *                     "inLedger": 23,
 * *                     "ledger_index": 23
 */
public class XRP_TX extends BaseDao implements Serializable {
    @Json(name = "")
    public String Account;
    @Json(name = "")
    public String Amount;
    @Json(name = "")
    public String Destination;
    @Json(name = "")
    public String DestinationTag;
    @Json(name = "")
    public String Fee;
    @Json(name = "")
    public String Flags;
    @Json(name = "")
    public String InvoiceID;

    @Json(name = "")
    public String LastLedgerSequence;
    @Json(name = "")
    public String Sequence;
    @Json(name = "")
    public String SigningPubKey;
    @Json(name = "")
    public String TransactionType;
    @Json(name = "")
    public String TxnSignature;
    @Json(name = "")
    public String hash;


    @Json(name = "")
    public String date;
    @Json(name = "")
    public String inLedger;
    @Json(name = "")
    public String ledger_index;

    /**
     * The rippled server and its APIs represent time as an unsigned integer.
     * This number measures the number of seconds since the "Ripple Epoch" of January 1, 2000 (00:00 UTC).
     * This is like the way the Unix epoch  works, except the Ripple Epoch is 946684800 seconds after the Unix Epoch.
     *
     * @return
     */
    public String getTime() {
        long time = 946656000L; //January 1, 2000 (00:00 UTC). 946684800
        try {
            time += Long.parseLong(date);
        } catch (Exception e) {
            LogRipple.error("exception", e);
        }
        return date;//DateUtils.formatDateTime(time * 1000 + 8 * 60 * 60 * 1000, "yyyy-MM-dd HH:mm:ss");
    }

}

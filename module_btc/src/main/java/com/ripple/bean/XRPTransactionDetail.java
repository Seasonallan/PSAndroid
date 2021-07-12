package com.ripple.bean;

import com.ripple.LogRipple;
import com.ripple.bean.transaction.XRPAccount_Meta;

import java.io.Serializable;

/**
 */
public class XRPTransactionDetail extends BaseResult implements Serializable {
    public String Account;
    public String Amount;
    public String Destination;
    public String DestinationTag;
    public String Fee;
    public String Flags;
    public String InvoiceID;

    public String LastLedgerSequence;
    public String Sequence;
    public String SigningPubKey;
    public String TransactionType;
    public String TxnSignature;
    public String hash;


    public String date;
    public String inLedger;
    public String ledger_index;


    //获取交易信息
    public XRPAccount_Meta meta;
    public boolean validated;

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

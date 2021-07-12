package com.ripple.bean;

import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 * "Account": "rDjW7L93g1rMbSEKTiJ7RV957bHQkBqrKt",
 * "Balance": "989999830",
 * "Flags": 8388608,
 * "LedgerEntryType": "AccountRoot",
 * "OwnerCount": 0,
 * "PreviousTxnID": "BB1CFBE04CD7E2A562907E8BBB6F5BCF04EDCDA09971AC1E95656032B587FA43",
 * "PreviousTxnLgrSeq": 26,
 * "Sequence": 24,
 * "index": "F2304AB28D4F180B26FF6F81B92758C201B7D56C7EE79159A16B77845FD28702"
 */
public class XRP_ACCOUNT extends BaseDao implements Serializable {
    @Json(name = "")
    public String Account;
    @Json(name = "")
    public String Balance;
    @Json(name = "")
    public String Flags;
    @Json(name = "")
    public String LedgerEntryType;
    @Json(name = "")
    public String OwnerCount;
    @Json(name = "")
    public String PreviousTxnID;
    @Json(name = "")
    public String PreviousTxnLgrSeq;

    @Json(name = "")
    public String Sequence;
    @Json(name = "")
    public String index;

}

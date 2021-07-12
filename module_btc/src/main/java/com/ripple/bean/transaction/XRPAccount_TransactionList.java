package com.ripple.bean.transaction;

import com.ripple.bean.BaseResult;
import com.ripple.bean.base.Json;

import java.io.Serializable;
import java.util.List;

/**
 * "account": "rD1Wjng7VakncxMtLCL9CT9EU8M8jXyYsv",
 * "ledger_index_max": 23,
 * "ledger_index_min": 2,
 * "marker": {
 * "ledger": 3,
 * "seq": 1
 * },
 * "limit": 5,
 * "transactions": [
 * {
 * }]
 */
public class XRPAccount_TransactionList extends BaseResult implements Serializable {
    @Json(name = "")
    public String account;
    @Json(name = "")
    public String ledger_index_max;
    @Json(name = "")
    public String ledger_index_min;
    @Json(name = "")
    public XRPMarker marker;

    @Json(name = "")
    public String limit;

    @Json(name = "transactions", className = XRPAccount_Transaction.class)
    public List<XRPAccount_Transaction> transactions;

}

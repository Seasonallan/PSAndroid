package com.ripple.bean;


import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 "account_data": {

 },
 "ledger_current_index": 29,
 "status": "success",
 "validated": false
 */
public class XRPAccount extends BaseResult  implements Serializable {
    @Json(name = "")
    public XRP_ACCOUNT account_data;
    @Json(name = "")
    public int ledger_current_index;
    @Json(name = "")
    public boolean validated;
}

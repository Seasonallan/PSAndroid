package com.ripple.bean;

import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 "median_level": "128000",
 "minimum_level": "256",
 "open_ledger_level": "256",
 "reference_level": "256"
 */
public class XRP_LEVELS extends BaseDao implements Serializable {
    @Json(name = "")
    public String median_level;
    @Json(name = "")
    public String minimum_level;
    @Json(name = "")
    public String open_ledger_level;
    @Json(name = "")
    public String reference_level;
}

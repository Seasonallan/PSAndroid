package com.ripple.bean;

import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 * "current_ledger_size": "0",
 * "current_queue_size": "0",
 * "drops": {
 * "base_fee": "10",
 * "median_fee": "5000",
 * "minimum_fee": "10",
 * "open_ledger_fee": "10"
 * },
 * "expected_ledger_size": "1000",
 * "ledger_current_index": 33,
 * "levels": {
 * "median_level": "128000",
 * "minimum_level": "256",
 * "open_ledger_level": "256",
 * "reference_level": "256"
 * },
 * "max_queue_size": "20000",
 * "status": "success"
 */
public class XRPFee extends BaseResult implements Serializable {
    @Json(name = "")
    public String current_ledger_size;
    @Json(name = "")
    public String current_queue_size;
    @Json(name = "")
    public XRP_DROPS drops;
    @Json(name = "")
    public String expected_ledger_size;
    @Json(name = "")
    public String ledger_current_index;
    @Json(name = "")
    public XRP_LEVELS levels;
    @Json(name = "")
    public String max_queue_size;


}

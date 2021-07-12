package com.ripple.bean.transaction;

import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 *
 */
public class XRPMarker extends BaseDao implements Serializable {
    @Json(name = "")
    public int ledger;
    @Json(name = "")
    public int seq;
}

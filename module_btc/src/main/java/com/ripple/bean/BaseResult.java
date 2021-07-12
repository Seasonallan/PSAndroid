package com.ripple.bean;

import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

public class BaseResult extends BaseDao implements Serializable {
    @Json(name = "")
    public String deprecated;

    @Json(name = "")
    public String error;

    @Json(name = "")
    public int error_code;

    @Json(name = "")
    public String error_message;

    @Json(name = "")
    public String status;

    public boolean isSuccess() {
        return "success".equals(status);
    }
}
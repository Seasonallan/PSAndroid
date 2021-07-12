package com.ripple.bean;

import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 * "engine_result": "tesSUCCESS",
 * "engine_result_code": 0,
 * "engine_result_message": "The transaction was applied. Only final in a validated ledger.",
 * <p>
 * "tx_blob": "120000228000000024000000042E000000015011B4976E3BF8FFEDC0DFD82E974A40C46C3E5B542C714AA6A2FB4B611AB244E04E61400000003B9ACA0068400000000000000A73210330E7FC9D56BB25D6893BA3F317AE5BCF33B3291BD63DB32654A313222F7FD02074473045022100A3059E444ADEA2A02B102CC5CA1A2146372B5C265B63D196676FC1B034119DC70220671A9EAB838B224DA6F1E737AD80C7A15666C88AA232C484F8E5A5B2B7B4A39B8114B5F762798A53D543A014CAF8B297CFF8F2F937E883148BAB94C49DF4EC1171ECDD1C1DB7A8678EE222BC",
 * "tx_json": {
 * <p>
 * }
 */
public class XRPTransaction extends BaseResult  implements Serializable {
    @Json(name = "")
    public String engine_result;
    @Json(name = "")
    public String engine_result_code;
    @Json(name = "")
    public String engine_result_message;
    @Json(name = "")
    public String tx_blob;

    @Json(name = "")
    public XRP_TX tx_json;


    public boolean isSuccess(){
        return "tesSUCCESS".equals(engine_result);
    }
}

package com.ripple.bean.transaction;

import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;
import java.util.List;

/**
 * "meta": {
 *                     "AffectedNodes": [
 *                         {
 *                             "ModifiedNode": {
 *                                 "FinalFields": {
 *                                     "Account": "rNqKQoZzmYEXSafD2JU6pgNEp1BpJUU9oV",
 *                                     "Balance": "1000005000",
 *                                     "Flags": 0,
 *                                     "OwnerCount": 0,
 *                                     "Sequence": 3
 *                                 },
 *                                 "LedgerEntryType": "AccountRoot",
 *                                 "LedgerIndex": "3081229618A2822EF9BA1712BEDBCDDE34856373E99302F4FBE35881797E79D0",
 *                                 "PreviousFields": {
 *                                     "Balance": "1000004000"
 *                                 },
 *                                 "PreviousTxnID": "FF945C26164F83D8437699EB92002FC985A0432B7E4F6934BF5324A190E2CE0E",
 *                                 "PreviousTxnLgrSeq": 23
 *                             }
 *                         },
 *                         {
 *                             "ModifiedNode": {
 *                                 "FinalFields": {
 *                                     "Account": "rD1Wjng7VakncxMtLCL9CT9EU8M8jXyYsv",
 *                                     "Balance": "999994500",
 *                                     "Flags": 0,
 *                                     "OwnerCount": 0,
 *                                     "Sequence": 8
 *                                 },
 *                                 "LedgerEntryType": "AccountRoot",
 *                                 "LedgerIndex": "636738A99155061FF21C78FEA52DC4FBCCBC41D47B28D34E8356CB22F6E393A8",
 *                                 "PreviousFields": {
 *                                     "Balance": "999995600",
 *                                     "Sequence": 7
 *                                 },
 *                                 "PreviousTxnID": "FF945C26164F83D8437699EB92002FC985A0432B7E4F6934BF5324A190E2CE0E",
 *                                 "PreviousTxnLgrSeq": 23
 *                             }
 *                         }
 *                     ],
 *                     "TransactionIndex": 1,
 *                     "TransactionResult": "tesSUCCESS",
 *                     "delivered_amount": "1000"
 *                 },
 */
public class    XRPAccount_Meta extends BaseDao implements Serializable {
    @Json(name = "")
    public String TransactionIndex;
    @Json(name = "")
    public String TransactionResult;
    @Json(name = "")
    public String delivered_amount;
    @Json(name = "AffectedNodes", className = XRPAccount_AffectedNode.class)
    public List<XRPAccount_AffectedNode> AffectedNodes;
}

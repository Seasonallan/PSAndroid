package com.ripple.bean.transaction;

import com.ripple.bean.XRP_TX;
import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

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
 *                 "tx": {
 *                     "Account": "rD1Wjng7VakncxMtLCL9CT9EU8M8jXyYsv",
 *                     "Amount": "1000",
 *                     "Destination": "rNqKQoZzmYEXSafD2JU6pgNEp1BpJUU9oV",
 *                     "DestinationTag": 1,
 *                     "Fee": "100",
 *                     "Flags": 2147483648,
 *                     "LastLedgerSequence": 24,
 *                     "Sequence": 7,
 *                     "SigningPubKey": "03DC60EB76FD89DCB5CAFF4CC90F7CD635372A81C9D1FC83079995D77AF2740D1E",
 *                     "TransactionType": "Payment",
 *                     "TxnSignature": "30450221008F1EE23DBB30E121C620036D1FE865B8DDBAEF6D6431509125C96265C682439A0220108BDA757BCE5C6000702E59D1D41F4F7FD13F012DD1B18227BDE91E1B29667D",
 *                     "date": 652529510,
 *                     "hash": "4BB79AE337A4792F04A82C5E7F0372B7A91CA62581C87FDE2D7E5693A85B76CB",
 *                     "inLedger": 23,
 *                     "ledger_index": 23
 *                 },
 *                 "validated": true
 */
public class XRPAccount_Transaction extends BaseDao implements Serializable {
    @Json(name = "")
    public XRPAccount_Meta meta;
    @Json(name = "")
    public XRP_TX tx;
    @Json(name = "")
    public boolean validated;
}

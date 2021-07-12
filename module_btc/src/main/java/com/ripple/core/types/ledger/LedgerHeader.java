package com.ripple.core.types.ledger;

import com.ripple.core.binary.STReader;
import com.ripple.core.coretypes.RippleDate;
import com.ripple.core.coretypes.hash.HalfSha512;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.hash.prefixes.HashPrefix;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.coretypes.uint.UInt64;
import com.ripple.core.coretypes.uint.UInt8;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesSink;

import java.util.Date;

public class LedgerHeader {
    // Always 0x4C475200 (LWR) (Secures signed objects)
    public UInt32  version = HashPrefix.ledgerMaster.uInt32;

    public UInt32  sequence;        // Ledger Sequence (0 for genesis ledger)
    public UInt64  totalXRP;        //
    public Hash256 previousLedger;  // The hash of the previous ledger (0 for genesis ledger)
    public Hash256 transactionHash; // The hash of the transaction tree's root node.
    public Hash256 stateHash;       // The hash of the state tree's root node.
    public UInt32  parentCloseTime; // The time the previous ledger closed
    public UInt32  closeTime;       // UTC minute ledger closed encoded as seconds since 1/1/2000 (or 0 for genesis ledger)
    public UInt8 closeResolution; // The resolution (in seconds) of the close time
    public UInt8 closeFlags;      // Flags

    public Date closeDate;

    public static LedgerHeader fromParser(BinaryParser parser) {
        return fromReader(new STReader(parser));
    }
    public static LedgerHeader fromReader(STReader reader) {
        LedgerHeader ledger = new LedgerHeader();

        ledger.sequence = reader.uInt32();
        ledger.totalXRP = reader.uInt64();
        ledger.previousLedger = reader.hash256();
        ledger.transactionHash= reader.hash256();
        ledger.stateHash = reader.hash256();
        ledger.parentCloseTime = reader.uInt32();
        ledger.closeTime = reader.uInt32();
        ledger.closeResolution = reader.uInt8();
        ledger.closeFlags = reader.uInt8();

        ledger.closeDate = RippleDate.fromSecondsSinceRippleEpoch(ledger.closeTime);

        return ledger;
    }

    public void toBytesSink(BytesSink sink) {
        sequence.toBytesSink(sink);
        totalXRP.toBytesSink(sink);
        previousLedger.toBytesSink(sink);
        transactionHash.toBytesSink(sink);
        stateHash.toBytesSink(sink);
        parentCloseTime.toBytesSink(sink);
        closeTime.toBytesSink(sink);
        closeResolution.toBytesSink(sink);
        closeFlags.toBytesSink(sink);
    }

    public Hash256 hash() {
        HalfSha512 half = HalfSha512.prefixed256(HashPrefix.ledgerMaster);
        toBytesSink(half);
        return half.finish();
    }

}

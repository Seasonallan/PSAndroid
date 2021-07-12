package com.ripple.core.coretypes;

import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.sle.LedgerEntry;
import com.ripple.core.types.known.sle.LedgerHashes;
import com.ripple.core.types.known.sle.entries.AccountRoot;
import com.ripple.core.types.known.sle.entries.DirectoryNode;
import com.ripple.core.types.known.sle.entries.Offer;
import com.ripple.core.types.known.sle.entries.OfferDirectory;
import com.ripple.core.types.known.sle.entries.RippleState;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.result.AffectedNode;
import com.ripple.core.types.known.tx.result.TransactionMeta;
import com.ripple.core.types.known.tx.txns.AccountSet;
import com.ripple.core.types.known.tx.txns.OfferCancel;
import com.ripple.core.types.known.tx.txns.OfferCreate;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.core.types.known.tx.txns.TicketCancel;
import com.ripple.core.types.known.tx.txns.TicketCreate;
import com.ripple.core.types.known.tx.txns.TrustSet;

public class STObjectFormatter {
    public static STObject doFormatted(STObject source) {
        // This would need to go before the test that just checks
        // for ledgerEntryType
        if (AffectedNode.isAffectedNode(source)) {
            return new AffectedNode(source);
        }

        if (TransactionMeta.isTransactionMeta(source)) {
            TransactionMeta meta = new TransactionMeta();
            meta.fields = source.fields;
            return meta;
        }

        LedgerEntryType ledgerEntryType = STObject.ledgerEntryType(source);
        if (ledgerEntryType != null) {
            return ledgerFormatted(source, ledgerEntryType);
        }

        TransactionType transactionType = STObject.transactionType(source);
        if (transactionType != null) {
            return transactionFormatted(source, transactionType);
        }

        return source;
    }

    private static STObject transactionFormatted(STObject source, TransactionType transactionType) {
        STObject constructed = null;
        switch (transactionType) {
            case Invalid:
            case SuspendedPaymentCreate:
            case SuspendedPaymentFinish:
            case SuspendedPaymentCancel:
            case SetRegularKey:
            case NickNameSet:
            case EnableAmendment:
            case SetFee:
            case SignerListSet:
            case unused:
                break;
            case Payment:
                constructed = new Payment();
                break;
            case AccountSet:
                constructed = new AccountSet();
                break;
            case OfferCreate:
                constructed = new OfferCreate();
                break;
            case OfferCancel:
                constructed = new OfferCancel();
                break;
            case TicketCreate:
                constructed = new TicketCreate();
                break;
            case TicketCancel:
                constructed = new TicketCancel();
                break;
            case TrustSet:
                constructed = new TrustSet();
                break;

        }
        if (constructed == null) {
            constructed = new Transaction(transactionType);
        }

        constructed.fields = source.fields;
        return constructed;

    }

    private static STObject ledgerFormatted(STObject source, LedgerEntryType ledgerEntryType) {
        STObject constructed = null;
        switch (ledgerEntryType) {
            case Offer:
                constructed = new Offer();
                break;
            case RippleState:
                constructed = new RippleState();
                break;
            case AccountRoot:
                constructed = new AccountRoot();
                break;
            case Invalid:
            case FeeSettings:
            case Amendments:
            case Contract:
            case GeneratorMap:
                break;
            case DirectoryNode:
                if (source.has(Field.Owner)) {
                    constructed = new DirectoryNode();
                } else {
                    constructed = new OfferDirectory();
                }

                break;
            case LedgerHashes:
                constructed = new LedgerHashes();
                break;
        }
        if (constructed == null) {
            constructed = new LedgerEntry(ledgerEntryType);
        }
        constructed.fields = source.fields;
        return constructed;
    }
}

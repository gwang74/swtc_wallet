package com.android.jtwallet.core.types.known.tx.txns;

import com.android.jtwallet.core.serialized.enums.TransactionType;
import com.android.jtwallet.core.types.known.tx.Transaction;

public class TicketCreate extends Transaction {
    public TicketCreate() {
        super(TransactionType.TicketCreate);
    }
}

package com.android.jtwallet.core.types.shamap;
import com.android.jtwallet.core.types.known.tx.result.TransactionResult;

public interface TransactionResultVisitor {
    public void onTransaction(TransactionResult tx);
}

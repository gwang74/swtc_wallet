package com.android.jtwallet.core.types.shamap;

import com.android.jtwallet.core.types.known.sle.LedgerEntry;

public interface LedgerEntryVisitor {
    public void onEntry(LedgerEntry entry);
}

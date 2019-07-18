package com.android.jtwallet.core.types.known.tx.txns;

import com.android.jtwallet.core.coretypes.Amount;
import com.android.jtwallet.core.fields.Field;
import com.android.jtwallet.core.serialized.enums.TransactionType;
import com.android.jtwallet.core.types.known.tx.Transaction;

public class RelationSet extends Transaction {
	
    public RelationSet() {
        super(TransactionType.RelationSet);
    }
    

    public Amount limitAmount() {return get(Amount.LimitAmount);}
    public void limitAmount(Amount val) {put(Field.LimitAmount, val);}
    
}
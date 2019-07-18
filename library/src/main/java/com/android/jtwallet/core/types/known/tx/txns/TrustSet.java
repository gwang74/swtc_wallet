package com.android.jtwallet.core.types.known.tx.txns;

import com.android.jtwallet.client.Remote;
import com.android.jtwallet.core.coretypes.Amount;
import com.android.jtwallet.core.coretypes.uint.UInt32;
import com.android.jtwallet.core.fields.Field;
import com.android.jtwallet.core.serialized.enums.TransactionType;
import com.android.jtwallet.core.types.known.tx.Transaction;

public class TrustSet extends Transaction {
	
    public TrustSet() {
        super(TransactionType.TrustSet);
    }
    
    public TrustSet(Remote remote) {
		super(TransactionType.TrustSet);
		this.remote = remote;
	}
    
    private Remote remote = null;

    public UInt32 qualityIn() {return get(UInt32.QualityIn);}
    public UInt32 qualityOut() {return get(UInt32.QualityOut);}
    public Amount limitAmount() {return get(Amount.LimitAmount);}
    public void qualityIn(UInt32 val) {put(Field.QualityIn, val);}
    public void qualityOut(UInt32 val) {put(Field.QualityOut, val);}
    public void limitAmount(Amount val) {put(Field.LimitAmount, val);}
}
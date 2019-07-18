package com.android.jtwallet.core.binary;

import com.android.jtwallet.core.coretypes.hash.Hash256;
import com.android.jtwallet.core.serialized.BinarySerializer;
import com.android.jtwallet.core.serialized.BytesSink;
import com.android.jtwallet.core.serialized.SerializedType;
import com.android.jtwallet.core.types.known.sle.LedgerEntry;
import com.android.jtwallet.core.types.known.tx.result.TransactionResult;

public class STWriter implements BytesSink {
    BytesSink sink;
    BinarySerializer serializer;
    public STWriter(BytesSink bytesSink) {
        serializer = new BinarySerializer(bytesSink);
        sink = bytesSink;
    }
    public void write(SerializedType obj) {
        obj.toBytesSink(sink);
    }
    public void writeVl(SerializedType obj) {
        serializer.addLengthEncoded(obj);
    }

    @Override
    public void add(byte aByte) {
        sink.add(aByte);
    }

    @Override
    public void add(byte[] bytes) {
        sink.add(bytes);
    }

    public void write(TransactionResult result) {
        write(result.hash);
        writeVl(result.txn);
        writeVl(result.meta);
    }

    public void write(Hash256 hash256, LedgerEntry le) {
        write(hash256);
        writeVl(le);
    }
}

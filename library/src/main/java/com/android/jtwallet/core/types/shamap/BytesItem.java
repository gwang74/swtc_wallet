package com.android.jtwallet.core.types.shamap;

import com.android.jtwallet.core.coretypes.hash.prefixes.Prefix;
import com.android.jtwallet.core.serialized.BytesSink;

public class BytesItem extends ShaMapItem<byte[]> {
    private byte[] item;

    public BytesItem(byte[] item) {
        this.item = item;
    }

    @Override
    void toBytesSink(BytesSink sink) {
        sink.add(item);
    }

    @Override
    public ShaMapItem<byte[]> copy() {
        return this;
    }

    @Override
    public byte[] value() {
        return item;
    }

    @Override
    public Prefix hashPrefix() {
        return new Prefix() {
            @Override
            public byte[] bytes() {
                return new byte[0];
            }
        };
    }
}

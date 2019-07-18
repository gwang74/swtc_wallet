package com.android.jtwallet.core.types.shamap;

import com.android.jtwallet.core.coretypes.hash.prefixes.Prefix;
import com.android.jtwallet.core.serialized.BytesSink;

abstract public class ShaMapItem<T> {
    abstract void toBytesSink(BytesSink sink);
    public abstract ShaMapItem<T> copy();
    public abstract T value();
    public abstract Prefix hashPrefix();
}

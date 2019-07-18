package com.android.jtwallet.core.serialized.enums;

import java.util.TreeMap;

import com.android.jtwallet.core.fields.Type;
import com.android.jtwallet.core.serialized.BinaryParser;
import com.android.jtwallet.core.serialized.BytesSink;
import com.android.jtwallet.core.serialized.SerializedType;
import com.android.jtwallet.core.serialized.TypeTranslator;
import com.android.jtwallet.encoding.common.B16;

public enum TransactionType implements SerializedType {
	Invalid(-1), Payment(0), SuspendedPaymentCreate(1), // open
	SuspendedPaymentFinish(2), AccountSet(3), SuspendedPaymentCancel(4), // open
	SetRegularKey(5), NickNameSet(6), // open
	OfferCreate(7), OfferCancel(8), unused(9), TicketCreate(10), TicketCancel(11), SignerListSet(12), TrustSet(20),RelationSet(30), EnableAmendment(
	        100), SetFee(101);
	public int asInteger() {
		return ord;
	}
	
	final int ord;
	
	TransactionType(int i) {
		ord = i;
	}
	
	@Override
	public Type type() {
		return Type.UInt16;
	}
	
	static private TreeMap<Integer, TransactionType> byCode = new TreeMap<Integer, TransactionType>();
	static {
		for (Object a : TransactionType.values()) {
			TransactionType f = (TransactionType) a;
			byCode.put(f.ord, f);
		}
	}
	
	static public TransactionType fromNumber(Number i) {
		return byCode.get(i.intValue());
	}
	
	// SeralizedType interface
	@Override
	public byte[] toBytes() {
		// TODO: bytes
		return new byte[] { (byte) (ord >> 8), (byte) (ord & 0xFF) };
	}
	
	@Override
	public Object toJSON() {
		return toString();
	}
	
	@Override
	public String toHex() {
		return B16.toString(toBytes());
	}
	
	@Override
	public void toBytesSink(BytesSink to) {
		to.add(toBytes());
	}
	
	public static class Translator extends TypeTranslator<TransactionType> {
		@Override
		public TransactionType fromParser(BinaryParser parser, Integer hint) {
			byte[] read = parser.read(2);
			return fromNumber((read[0] << 8) | read[1]);
		}
		
		@Override
		public TransactionType fromInteger(int integer) {
			return fromNumber(integer);
		}
		
		@Override
		public TransactionType fromString(String value) {
			return TransactionType.valueOf(value);
		}
	}
	
	public static Translator translate = new Translator();
}

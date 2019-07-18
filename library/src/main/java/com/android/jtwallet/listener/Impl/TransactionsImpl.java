package com.android.jtwallet.listener.Impl;

import java.util.HashMap;
import java.util.Map;

import com.android.jtwallet.client.Request;
import com.android.jtwallet.listener.RemoteInter;
import com.android.jtwallet.pubsub.Publisher;

public class TransactionsImpl extends Publisher implements RemoteInter,Runnable{

	public static interface events<T> extends Publisher.Callback<T> {}

	public static interface OnTransaction extends TransactionsImpl.events<String> {}

	private String message;

	public TransactionsImpl(String message) {
		this.message = message;
	}

	public TransactionsImpl() {
	}

	@Override
	public String submit(Request request) {
		Map params = new HashMap();
		params.put("streams", new String[] {"transactions"});		
		return request.submit(params);
	}

	@Override
	public void run() {
		emit(OnTransaction.class, message);
	}

}

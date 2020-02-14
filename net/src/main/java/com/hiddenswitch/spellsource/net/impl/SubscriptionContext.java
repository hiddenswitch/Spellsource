package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.util.DiffContext;
import io.reactivex.disposables.Disposable;

public interface SubscriptionContext<TRequest, TDocument> {
	UserId user();

	TRequest request();

	DiffContext<TDocument, Comparable<String>> client();

	void close();

	void addDisposable(Disposable disposable);
}

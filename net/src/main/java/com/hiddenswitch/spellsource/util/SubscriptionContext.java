package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.impl.UserId;
import io.reactivex.disposables.Disposable;

public interface SubscriptionContext<TRequest, TDocument> {
	UserId user();

	TRequest request();

	DiffContext<TDocument, Comparable<String>> client();

	void close();

	void addDisposable(Disposable disposable);
}

package com.hiddenswitch.spellsource.util;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.Map;

public interface AddedChangedRemoved<K, V> extends Disposable {
	Observable<Map.Entry<K, V>> added();

	Observable<Map.Entry<K, V>> changed();

	Observable<Map.Entry<K, V>> removed();

	@Override
	default void dispose() {
	}

	@Override
	default boolean isDisposed() {
		return true;
	}
}

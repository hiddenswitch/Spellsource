package com.hiddenswitch.spellsource.util;

import io.reactivex.Observable;

import java.util.Map;

public interface AddedChangedRemoved<K, V> {
	Observable<Map.Entry<K, V>> added();

	Observable<Map.Entry<K, V>> changed();

	Observable<Map.Entry<K, V>> removed();
}

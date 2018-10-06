package com.hiddenswitch.spellsource.concurrent.impl;

import com.hiddenswitch.spellsource.util.AddedChangedRemoved;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.vertx.core.Vertx;
import io.vertx.reactivex.RxHelper;

import java.util.Map;

abstract class AbstractAddedChangedRemoved<K, V> implements AddedChangedRemoved<K, V> {
	final Subject<Map.Entry<K, V>> added = PublishSubject.create();
	final Subject<Map.Entry<K, V>> changed = PublishSubject.create();
	final Subject<Map.Entry<K, V>> removed = PublishSubject.create();

	public Observable<Map.Entry<K, V>> added() {
		return added
				.observeOn(RxHelper.scheduler(Vertx.currentContext()));
	}

	public Observable<Map.Entry<K, V>> changed() {
		return changed
				.observeOn(RxHelper.scheduler(Vertx.currentContext()));
	}

	public Observable<Map.Entry<K, V>> removed() {
		return removed
				.observeOn(RxHelper.scheduler(Vertx.currentContext()));
	}
}
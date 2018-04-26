package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.ICondition;
import com.hazelcast.core.ILock;
import io.vertx.core.*;

import static com.hiddenswitch.spellsource.util.Sync.invoke0;
import static io.vertx.ext.sync.Sync.awaitResult;

class SuspendableHazelcastCondition implements SuspendableCondition, Closeable {
	private final String key;
	private ICondition condition;
	private ILock lock;

	SuspendableHazelcastCondition(String name) {
		this.key = "SuspendableEventBusCondition::consumer-" + name;
	}

	@Suspendable
	void init() throws SuspendExecution {
		Void t = awaitResult(h -> Vertx.currentContext().executeBlocking(fut -> {
			lock = SharedData.getHazelcastInstance().getLock(key);
			this.condition = lock.newCondition(key + "__condition");

			fut.complete();
		}, false, h));
	}

	@Override
	@Suspendable
	public long awaitMillis(long millis) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(fut -> {
			try {
				if (!lock.isLockedByCurrentThread()) {
					lock.lock();
				}
				fut.complete(this.condition.awaitNanos(millis * 1000));
			} catch (InterruptedException e) {
				fut.complete(-1L);
			} catch (IllegalMonitorStateException ex) {
				fut.fail(ex);
			}
		}, false, h));
	}

	@Override
	@Suspendable
	public void signal() {
		Void t = awaitResult(h -> Vertx.currentContext().executeBlocking(fut -> {
			try {
				if (!lock.isLockedByCurrentThread()) {
					lock.lock();
				}
				condition.signal();
			} catch (Throwable throwable) {
				fut.fail(throwable);
			}
			fut.complete();
		}, h));
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		completionHandler.handle(null);
	}
}

package com.hiddenswitch.spellsource.net.concurrent.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableQueue;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SuspendableAtomixQueue<V> implements SuspendableQueue<V>, AutoCloseable {
	public static final String SUSPENDABLE_ATOMIX_QUEUES = "SuspendableAtomixQueues";
	private static final Logger LOGGER = LoggerFactory.getLogger(SuspendableAtomixQueue.class);

	private final String name;
	private final SuspendableAtomixLoopingPollQueue<V> innerQueue;
	private final SuspendableEventBusCondition condition;


	public SuspendableAtomixQueue(String name) {
		this.name = name;
		this.innerQueue = new SuspendableAtomixLoopingPollQueue<V>(name);
		this.condition = new SuspendableEventBusCondition(getTopic());
	}

	@Override
	@Suspendable
	public boolean offer(@NotNull V item) {
		Objects.requireNonNull(item);
		if (!item.getClass().isPrimitive() && !(item instanceof ClusterSerializable)) {
			LOGGER.warn("offer {} {}: {} not cluster serializable", name, getNodeID(), item);
		}

		// TODO: We could shut down in the middle of this?
		boolean res = innerQueue.offer(item);

		if (res) {
			// Notify that we offered the queue
			condition.signalAll();
		} else {
			LOGGER.error("offer {} {}: failed to offer {}",name, getNodeID(), item);
		}

		return res;
	}

	public String getNodeID() {
		return ((VertxInternal) Vertx.currentContext().owner()).getNodeID();
	}

	@NotNull
	protected String getTopic() {
		return SUSPENDABLE_ATOMIX_QUEUES + "/" + name + "/notEmpty";
	}

	@Override
	@Suspendable
	public V poll(long timeout) throws InterruptedException, SuspendExecution {
		Context context = Vertx.currentContext();
		if (context == null) {
			throw new IllegalStateException("not on context");
		}

		// Always poll at least once
		V element = innerQueue.poll(0);
		while (timeout > 0 || element != null) {
			LOGGER.trace("vertx {} polled {}", getNodeID(), element);
			if (element != null) {
				return element;
			}

			timeout = condition.awaitMillis(timeout);

			// Poll
			element = innerQueue.poll(0);
		}

		return null;
	}

	@Override
	@Suspendable
	public V take() throws InterruptedException, SuspendExecution {
		V res = poll(Integer.MAX_VALUE / 2);
		if (res == null) {
			throw new InterruptedException();
		}
		return res;
	}

	@Override
	@Suspendable
	public void destroy() {
		Void v = awaitResult(condition::destroy);
		innerQueue.destroy();
	}

	@Override
	@Suspendable
	public void close() {
		condition.close((res) -> {
		});
	}
}

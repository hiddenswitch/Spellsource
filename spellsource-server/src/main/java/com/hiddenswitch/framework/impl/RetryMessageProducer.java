package com.hiddenswitch.framework.impl;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class RetryMessageProducer<T> implements MessageProducer<T> {
	private static final AtomicInteger ACTIVE_PRODUCERS = new AtomicInteger();

	private final Vertx vertx;
	private final MessageProducer<T> producer;
	private final int maxRetries;
	private final int intervalMillis;
	private final Deque<QueueItem<T>> waiting = new ArrayDeque<>();
	private QueueItem<T> inFlight;
	private boolean hasInFlight;
	private volatile boolean closed;
	private Long retryTimerId;
	private final Predicate<T> predicate;

	public RetryMessageProducer(MessageProducer<T> messageProducer, int maxRetries, int intervalMillis) {
		this(messageProducer, maxRetries, intervalMillis, v -> true);
	}

	public RetryMessageProducer(MessageProducer<T> messageProducer, int maxRetries, int intervalMillis, Predicate<T> predicate) {
		this.producer = messageProducer;
		this.maxRetries = maxRetries;
		this.intervalMillis = intervalMillis;
		this.vertx = Vertx.currentContext().owner();
		this.predicate = predicate;
		ACTIVE_PRODUCERS.incrementAndGet();
	}

	public static int getActiveCount() {
		return ACTIVE_PRODUCERS.get();
	}

	private Future<Void> write(QueueItem<T> item, int retries) {
		return producer
				.write(item.message)
				.recover(t -> {
					synchronized (this) {
						if (!closed && t instanceof ReplyException replyException && replyException.failureType() == ReplyFailure.NO_HANDLERS && retries > 0) {
							var promise = Promise.<Void>promise();
							retryTimerId = vertx.setTimer(intervalMillis, v -> {
								synchronized (RetryMessageProducer.this) {
									retryTimerId = null;
								}
								write(item, retries - 1)
										.onComplete(promise);
							});
							return promise.future();
						}
					}
					advanceQueue(item, t);
					return Future.failedFuture(t);
				})
				.compose(v -> {
					var next = advanceQueue(item, null);
					return next == null ? Future.succeededFuture() : write(next, next.initialRetries);
				});
	}

	private QueueItem<T> advanceQueue(QueueItem<T> item, Throwable failure) {
		QueueItem<T> next;
		synchronized (this) {
			item.completed = true;
			next = closed ? null : waiting.pollFirst();
			inFlight = next;
			hasInFlight = next != null;
		}
		if (failure != null) {
			item.promise.tryFail(failure);
			if (next != null) {
				write(next, next.initialRetries)
						.onFailure(com.hiddenswitch.framework.Environment.onFailure("write failures"));
			}
		} else {
			item.promise.tryComplete();
		}
		return next;
	}

	@Override
	public synchronized MessageProducer<T> deliveryOptions(DeliveryOptions options) {
		return producer.deliveryOptions(options);
	}

	@Override
	public String address() {
		return producer.address();
	}

	@Override
	public Future<Void> write(T body) {
		var result = Promise.<T>promise();
		var initialRetries = predicate.test(body) ? maxRetries : 0;
		var item = new QueueItem<>(body, result, initialRetries);
		boolean startWrite = false;
		synchronized (this) {
			if (closed) {
				return Future.failedFuture("closed");
			}
			if (!hasInFlight) {
				hasInFlight = true;
				inFlight = item;
				startWrite = true;
			} else {
				waiting.addLast(item);
			}
		}
		if (startWrite) {
			write(item, initialRetries)
					.onFailure(com.hiddenswitch.framework.Environment.onFailure("write failures"));
		}
		return result.future().map((Void) null);
	}

	public Future<Void> write(T body, DeliveryOptions options) {
		deliveryOptions(options);
		return write(body);
	}

	@Override
	public Future<Void> close() {
		Long timerId;
		Deque<QueueItem<T>> pending;
		synchronized (this) {
			if (closed) {
				return producer.close();
			}
			closed = true;
			ACTIVE_PRODUCERS.decrementAndGet();
			timerId = retryTimerId;
			retryTimerId = null;
			pending = new ArrayDeque<>(waiting);
			if (inFlight != null) {
				pending.addFirst(inFlight);
				inFlight = null;
			}
			waiting.clear();
			hasInFlight = false;
		}
		if (timerId != null) {
			vertx.cancelTimer(timerId);
		}
		for (var item : pending) {
			if (!item.completed) {
				item.completed = true;
				item.promise.tryFail("closed");
			}
		}
		return producer.close();
	}

	public synchronized void trim() {
		for (var item : waiting) {
			if (!item.completed) {
				item.completed = true;
				item.promise.tryFail("trimmed");
			}
		}
		waiting.clear();
	}

	private static final class QueueItem<X> {
		final X message;
		final Promise<X> promise;
		final int initialRetries;
		boolean completed;

		QueueItem(X message, Promise<X> promise, int initialRetries) {
			this.message = message;
			this.promise = promise;
			this.initialRetries = initialRetries;
		}
	}
}

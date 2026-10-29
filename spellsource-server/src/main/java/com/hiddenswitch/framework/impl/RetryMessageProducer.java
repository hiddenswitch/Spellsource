package com.hiddenswitch.framework.impl;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

public class RetryMessageProducer<T> implements MessageProducer<T> {

	private final Vertx vertx;
	private final MessageProducer<T> producer;
	private final int maxRetries;
	private final int intervalMillis;
	private final Deque<QueueItem<T>> waiting = new ArrayDeque<>();
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
	}

	private Future<Void> write(QueueItem<T> item, int retries) {
		if (item == null) {
			return Future.succeededFuture();
		}
		synchronized (this) {
			if (closed) {
				if (!item.completed) {
					item.completed = true;
					hasInFlight = false;
					item.promise.tryFail("closed");
				}
				return Future.failedFuture("closed");
			}
			hasInFlight = true;
		}

		return producer
				.write(item.message)
				.recover(t -> {
					// if there was no handler try again, up to retries - 1 times
					if (!closed && t instanceof ReplyException replyException && replyException.failureType() == ReplyFailure.NO_HANDLERS && retries > 0) {
						var promise = Promise.<Void>promise();
						synchronized (this) {
							if (closed) {
								if (!item.completed) {
									item.completed = true;
									hasInFlight = false;
									item.promise.tryFail("closed");
								}
								return Future.failedFuture("closed");
							}
							retryTimerId = vertx.setTimer(intervalMillis, v -> {
								synchronized (RetryMessageProducer.this) {
									retryTimerId = null;
									if (closed) {
										if (!item.completed) {
											item.completed = true;
											hasInFlight = false;
											item.promise.tryFail("closed");
										}
										promise.tryFail("closed");
										return;
									}
								}
								write(item, retries - 1)
										.onComplete(promise);
							});
						}
						return promise.future();
					} else {
						QueueItem<T> next;
						synchronized (this) {
							if (!item.completed) {
								item.completed = true;
							}
							next = closed ? null : waiting.pollFirst();
							if (next == null) {
								hasInFlight = false;
							}
						}
						item.promise.tryFail(t);
						if (next != null) {
							write(next, next.initialRetries)
									.onFailure(com.hiddenswitch.framework.Environment.onFailure("write failures"));
						}
						return Future.failedFuture(t);
					}
				})
				.compose(v -> {
					QueueItem<T> next;
					synchronized (this) {
						if (!item.completed) {
							item.completed = true;
						}
						next = closed ? null : waiting.pollFirst();
						if (next == null) {
							hasInFlight = false;
						}
					}
					item.promise.tryComplete();
					if (next == null) {
						return Future.succeededFuture();
					}
					return write(next, next.initialRetries);
				});
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
			closed = true;
			timerId = retryTimerId;
			retryTimerId = null;
			pending = new ArrayDeque<>(waiting);
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

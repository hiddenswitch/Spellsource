package com.hiddenswitch.framework.impl;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class RetryMessageProducer<T> implements MessageProducer<T> {

	private final Vertx vertx;
	private final MessageProducer<T> producer;
	private final int maxRetries;
	private final int intervalMillis;
	private final Deque<QueueItem<T>> waiting = new ArrayDeque<>();
	private final Set<QueueItem<T>> taken = new HashSet<>();
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
		taken.add(item);

		return producer
				.write(item.message)
				.recover(t -> {
					// if there was no handler try again, up to retries - 1 times
					if (t instanceof ReplyException replyException && replyException.failureType() == ReplyFailure.NO_HANDLERS && retries > 0) {
						var promise = Promise.<Void>promise();
						vertx.setTimer(intervalMillis, v ->
								write(item, retries - 1)
										.onComplete(promise));
						return promise.future();
					} else {
						if (taken.contains(item)) {
							taken.remove(item);
							item.promise.fail(t);
						}
						return Future.failedFuture(t);
					}
				})
				.compose(v -> {
					if (taken.contains(item)) {
						taken.remove(item);
						item.promise.complete();
					}
					return write(waiting.pollFirst(), retries);
				});
	}

	@Override
	public MessageProducer<T> deliveryOptions(DeliveryOptions options) {
		return producer.deliveryOptions(options);
	}

	@Override
	public String address() {
		return producer.address();
	}

	@Override
	public void write(T body, Handler<AsyncResult<Void>> handler) {
		write(body).onComplete(handler);
	}

	@Override
	public Future<Void> write(T body) {
		var result = Promise.<T>promise();
		waiting.addLast(new QueueItem<>(body, result));
		if (taken.isEmpty()) {
			write(waiting.pollFirst(), predicate.test(body) ? maxRetries : 0)
					.onFailure(com.hiddenswitch.framework.Environment.onFailure("write failures"));
		}
		return result.future().map((Void) null);
	}

	@Override
	public Future<Void> close() {
		return producer.close();
	}

	@Override
	public void close(Handler<AsyncResult<Void>> handler) {
		close().onComplete(handler);
	}

	public void trim() {
		waiting.clear();
	}

	record QueueItem<X>(X message, Promise<X> promise) {
	}
}

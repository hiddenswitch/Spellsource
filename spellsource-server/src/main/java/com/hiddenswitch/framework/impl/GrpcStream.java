package com.hiddenswitch.framework.impl;

import io.grpc.stub.StreamObserver;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class GrpcStream<T> implements StreamObserver<T> {

	private final Context context;
	private Handler<T> nextHandler;
	private Handler<Throwable> errorHandler;
	private Handler<Void> completedHandler;

	public GrpcStream() {
		this.context = Vertx.currentContext();
	}

	@Override
	public void onNext(T value) {
		if (nextHandler != null) {
			nextHandler.handle(value);
		}
	}

	@Override
	public void onError(Throwable t) {
		if (errorHandler != null) {
			errorHandler.handle(t);
		} else {
			context.exceptionHandler().handle(t);
		}
	}

	@Override
	public void onCompleted() {
		if (completedHandler != null) {
			completedHandler.handle((Void) null);
		}
	}

	public GrpcStream<T> setNextHandler(Handler<T> nextHandler) {
		this.nextHandler = nextHandler;
		return this;
	}

	public GrpcStream<T> setErrorHandler(Handler<Throwable> errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}

	public GrpcStream<T> setCompletedHandler(Handler<Void> completedHandler) {
		this.completedHandler = completedHandler;
		return this;
	}
}

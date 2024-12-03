package com.hiddenswitch.framework.virtual.concurrent;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

public abstract class AbstractVirtualThreadVerticle extends AbstractVerticle {
	private Context startingContext;
	private Context stoppingContext;

	@Override
	public final void init(Vertx vertx1, Context context1) {
		super.init(vertx1, context1);
	}

	@Override
	public final void start() throws Exception {
	}

	@Override
	public final void stop() throws Exception {
	}

	@Override
	public final void start(Promise<Void> startPromise) {
		setStartingContext(Vertx.currentContext());
		context.runOnContext(v -> {
			try {
				startVirtual();
			} catch (Throwable t) {
				startingContext().runOnContext(v2 -> startPromise.fail(t));
				return;
			}
			startingContext().runOnContext(v2 -> startPromise.complete());
		});
	}

	@Override
	public final void stop(Promise<Void> stopPromise) {
		setStoppingContext(Vertx.currentContext());
		context.runOnContext(v -> {
			try {
				stopVirtual();
			} catch (Throwable t) {
				stoppingContext().runOnContext(v2 -> stopPromise.fail(t));
				return;
			}
			stoppingContext().runOnContext(v2 -> stopPromise.complete());
		});
	}

	public void startVirtual() throws Exception {
	}

	public void stopVirtual() throws Exception {
	}

	protected Context startingContext() {
		return startingContext;
	}

	protected Context stoppingContext() {
		return stoppingContext;
	}

	protected void setStartingContext(Context startingContext) {
		this.startingContext = startingContext;
	}

	protected void setStoppingContext(Context stoppingContext) {
		this.stoppingContext = stoppingContext;
	}
}

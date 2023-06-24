package com.hiddenswitch.framework.virtual.concurrent;

import io.vertx.await.impl.EventLoopScheduler;
import io.vertx.await.impl.VirtualThreadContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

public abstract class AbstractVirtualThreadVerticle extends AbstractVerticle {

	@Override
	public final void init(Vertx vertx1, Context context1) {
		super.init(vertx1, context1);
		var context = (ContextInternal) context1;
		var scheduler = new EventLoopScheduler(context.nettyEventLoop());
		var vertx = (VertxInternal) vertx1;
		this.context = new VirtualThreadContext(vertx, context.nettyEventLoop(), vertx.getInternalWorkerPool(), vertx.getWorkerPool(), scheduler, context.getDeployment(), vertx.closeFuture(), Thread.currentThread().getContextClassLoader());
	}

	@Override
	public final void start() throws Exception {
	}

	@Override
	public final void stop() throws Exception {
	}

	@Override
	public final void start(Promise<Void> startPromise) throws Exception {
		context.runOnContext(v -> {
			try {
				virtualStart();
				startPromise.complete();
			} catch (Throwable t) {
				startPromise.fail(t);
			}
		});
	}

	@Override
	public final void stop(Promise<Void> stopPromise) throws Exception {
		context.runOnContext(v -> {
			try {
				virtualStop();
				stopPromise.complete();
			} catch (Throwable t) {
				stopPromise.fail(t);
			}
		});
	}

	public void virtualStart() throws Exception {
	}

	public void virtualStop() throws Exception {
	}

}

package com.hiddenswitch.framework.virtual.concurrent;

import com.hiddenswitch.diagnostics.Tracing;
import io.vertx.core.AbstractVerticle;

public abstract class AbstractVirtualThreadVerticle extends AbstractVerticle {
	@Override
	public final void start() throws Exception {
		Tracing.tracing(vertx);
		startVirtual();
	}

	@Override
	public final void stop() throws Exception {
		stopVirtual();
	}

	public void startVirtual() throws Exception {
	}

	public void stopVirtual() throws Exception {
	}
}

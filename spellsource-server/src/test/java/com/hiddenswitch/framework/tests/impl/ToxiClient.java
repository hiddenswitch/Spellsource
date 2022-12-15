package com.hiddenswitch.framework.tests.impl;

import com.hiddenswitch.framework.Client;
import io.vertx.core.Vertx;

public class ToxiClient extends Client {
	public ToxiClient(Vertx vertx) {
		super(vertx);
	}

	@Override
	protected int port() {
		return FrameworkTestBase.toxicGrpcProxy().getProxyPort();
	}

	@Override
	protected String host() {
		return FrameworkTestBase.toxicGrpcProxy().getContainerIpAddress();
	}
}

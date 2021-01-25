package com.hiddenswitch.framework.tests.impl;

import com.hiddenswitch.framework.Client;
import io.vertx.core.Vertx;

public class ToxiClient extends Client {
	public ToxiClient(Vertx vertx) {
		super(vertx);
	}

	@Override
	public String grpcAddress() {
		return FrameworkTestBase.toxicGrpcProxy().getContainerIpAddress() + ":" + FrameworkTestBase.toxicGrpcProxy().getProxyPort();
	}
}

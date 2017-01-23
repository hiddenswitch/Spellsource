package com.hiddenswitch.proto3.net.applications;

import com.hiddenswitch.proto3.net.impl.ServerImpl;
import io.vertx.core.Vertx;

/**
 * Created by bberman on 11/29/16.
 */
public class Embedded {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl());
	}
}

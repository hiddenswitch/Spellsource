package com.hiddenswitch.spellsource;

import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.NetworkInterface;
import java.net.SocketException;

@RunWith(VertxUnitRunner.class)
public class BroadcastTest {
	@Rule
	public RunTestOnContext contextRule = new RunTestOnContext();

	@Test
	public void testBroadcastCallResponse(TestContext context) throws SocketException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		final Vertx vertx = contextRule.vertx();
		vertx.exceptionHandler(context.exceptionHandler());
		final Broadcaster verticle = Broadcaster.create();
		final String expectedHostname = Gateway.getHostAddress();
		vertx.deployVerticle(verticle, context.asyncAssertSuccess(then -> {
			vertx.createDatagramSocket(new DatagramSocketOptions()
					.setReuseAddress(true)
					.setReusePort(true))
					.listen(verticle.getMulticastPort(), "0.0.0.0", context.asyncAssertSuccess(listening -> {
						try {
							listening.listenMulticastGroup(verticle.getMulticastAddress(), Gateway.mainInterface().getName(), null, context.asyncAssertSuccess(socket -> {
								final Async async = context.async();
								socket.handler(packet -> {
									final String packetData = packet.data().toString();
									if (packetData.equals(verticle.getClientCall())) {
										return;
									}
									context.assertNotEquals(packetData, verticle.getResponsePrefix() + "http://127.0.0.1:" + Integer.toString(Port.port()) + "/");
									context.assertNotEquals(packetData, verticle.getResponsePrefix() + "http://0.0.0.0:" + Integer.toString(Port.port()) + "/");
									context.assertEquals(packetData, verticle.getResponsePrefix() + "http://" + expectedHostname + ":" + Integer.toString(Port.port()) + "/");
									async.complete();
								});
								socket.send(verticle.getClientCall(), verticle.getMulticastPort(), verticle.getMulticastAddress(), context.asyncAssertSuccess());
							}));
						} catch (SocketException e) {
							context.fail("Could not retrieve main network interface.");
						}
					}));

		}));
	}
}

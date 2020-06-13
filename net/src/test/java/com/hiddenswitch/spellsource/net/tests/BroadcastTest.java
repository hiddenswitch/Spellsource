package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.net.Broadcaster;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.net.Gateway;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(VertxUnitRunner.class)
public class BroadcastTest {
	private static Logger LOGGER = LoggerFactory.getLogger(BroadcastTest.class);
	@Rule
	public RunTestOnContext contextRule = new RunTestOnContext();

	@Test
	public void testBroadcastCallResponse(TestContext context) {
		// On Travis, UDP broadcast is disabled.
		if (System.getenv().containsKey("CI")) {
			LOGGER.warn("UDP broadcast will not test correctly in the Travis environment.");
			return;
		}

		System.setProperty("java.net.preferIPv4Stack", "true");
		Vertx vertx = contextRule.vertx();
		vertx.exceptionHandler(context.exceptionHandler());
		Broadcaster verticle = Broadcaster.create();
		String expectedHostname = Gateway.getHostIpAddress();
		Async async = context.async();

		vertx.deployVerticle(verticle, context.asyncAssertSuccess(then -> {
			vertx.createDatagramSocket(new DatagramSocketOptions()
					.setReuseAddress(true)
					.setReusePort(true))
					.listen(verticle.getMulticastPort() + 1, "0.0.0.0", context.asyncAssertSuccess(socket -> {
						socket.handler(packet -> {
							String packetData = packet.data().toString();
							context.assertNotEquals(packetData, verticle.getResponsePrefix() + "http://127.0.0.1:" + Configuration.apiGatewayPort() + "/");
							context.assertNotEquals(packetData, verticle.getResponsePrefix() + "http://0.0.0.0:" + Configuration.apiGatewayPort() + "/");
							context.assertEquals(packetData, verticle.getResponsePrefix() + "http://" + expectedHostname + ":" + Configuration.apiGatewayPort() + "/");
							async.complete();
						});

						var testHandler = context.<DatagramSocket>asyncAssertSuccess();
						vertx.setTimer(2000L, v -> {
							socket.send(verticle.getClientCall(), verticle.getMulticastPort(), verticle.getMulticastAddress(), testHandler);
							LOGGER.info("BroadcastTest: packet sent");
						});
					}));
		}));
	}

}

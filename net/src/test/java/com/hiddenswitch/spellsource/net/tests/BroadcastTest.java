package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.net.Broadcaster;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.net.Gateway;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class BroadcastTest {
	private static Logger LOGGER = LoggerFactory.getLogger(BroadcastTest.class);

	@Test
	@Timeout(4500)
	public void testBroadcastCallResponse(VertxTestContext context, Vertx vertx) throws InterruptedException {
		// On Travis, UDP broadcast is disabled.
		if (System.getenv().containsKey("CI")) {
			LOGGER.warn("UDP broadcast will not test correctly in the Travis environment.");
			return;
		}

		System.setProperty("java.net.preferIPv4Stack", "true");
		vertx.exceptionHandler(context::failNow);
		var verticle = Broadcaster.create();
		var expectedHostname = Gateway.getHostIpAddress();
		var async = context.checkpoint();

		vertx.deployVerticle(verticle, context.succeeding(then -> {
			vertx.createDatagramSocket(new DatagramSocketOptions()
					.setReuseAddress(true)
					.setReusePort(true))
					.listen(verticle.getMulticastPort() + 1, "0.0.0.0", context.succeeding(socket -> {
						socket.handler(packet -> {
							context.verify(() -> {
								String packetData = packet.data().toString();
								assertNotEquals(packetData, verticle.getResponsePrefix() + "http://127.0.0.1:" + Configuration.apiGatewayPort() + "/");
								assertNotEquals(packetData, verticle.getResponsePrefix() + "http://0.0.0.0:" + Configuration.apiGatewayPort() + "/");
								assertEquals(packetData, verticle.getResponsePrefix() + "http://" + expectedHostname + ":" + Configuration.apiGatewayPort() + "/");
							});
							async.flag();
						});

						var testHandler = context.<DatagramSocket>succeeding();
						vertx.setTimer(2000L, v -> {
							socket.send(verticle.getClientCall(), verticle.getMulticastPort(), verticle.getMulticastAddress(), testHandler);
							LOGGER.info("BroadcastTest: packet sent");
						});
					}));
		}));
	}
}

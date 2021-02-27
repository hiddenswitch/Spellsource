package com.hiddenswitch.framework;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Broadcaster extends AbstractVerticle implements Verticle {
	private final Logger LOGGER = LoggerFactory.getLogger(Broadcaster.class);
	private final String multicastAddress = "230.0.0.1";
	private final String clientCall = "LOXX";
	private final String responsePrefix = "SPLL";
	private Map<NetworkInterface, DatagramSocket> hostSockets = new HashMap<>();

	/**
	 * Creates a new default Broadcaster.
	 *
	 * @return
	 */
	public static Broadcaster create() {
		return new Broadcaster();
	}

	@Override
	public void start(Promise<Void> startFuture) throws Exception {
		var networkInterface = Environment.mainInterface();
		if (networkInterface == null) {
			startFuture.fail("No valid network interface found.");
			return;
		}
		LOGGER.info("start: Broadcasting {}", networkInterface.toString());
		hostSockets.put(networkInterface, createDatagramSocket(networkInterface, startFuture));
	}

	private DatagramSocket createDatagramSocket(final NetworkInterface networkInterface, Promise<Void> isListening) throws SocketException {
		return vertx.createDatagramSocket(new DatagramSocketOptions()
				.setReuseAddress(true)
				.setReusePort(true))
				.listen(getMulticastPort(), "0.0.0.0", next -> {
					var socket = next.result();
					socket.listenMulticastGroup(multicastAddress, networkInterface.getName(), null, multicastListen -> {
						if (multicastListen.failed()) {
							LOGGER.error("createDatagramSocket: Failed to listen to multicast group", multicastListen.cause());
							isListening.fail(multicastListen.cause());
						}
						socket.handler(packet -> {
							if (!packet.data().getString(0, clientCall.length()).equals(clientCall)) {
								return;
							}

							LOGGER.info("createDatagramSocket: Replying to datagram received from " + packet.sender().toString());
							// Reply with the local base path
							var host = Environment.getHostIpAddress();
							socket.send(getResponsePrefix() + "http://" + host + ":" + Gateway.grpcPort() + "/", packet.sender().port(), packet.sender().host(), Promise.promise());
						});
						isListening.complete();
						LOGGER.info("Broadcaster listening on port " + multicastAddress + ":" + getMulticastPort());
					});
				});
	}

	@Override
	public void stop(Promise<Void> stopFuture) throws Exception {
		CompositeFuture.join(hostSockets.values().stream()
				.map(DatagramSocket::close)
				.collect(Collectors.toList()))
				.map((Void) null)
				.onComplete(stopFuture);
	}

	/**
	 * Retrieves the configured address used for UDP multicasting.
	 *
	 * @return
	 */
	public String getMulticastAddress() {
		return multicastAddress;
	}

	/**
	 * Retrieves the message expected from clients to respond to.
	 *
	 * @return
	 */
	public String getClientCall() {
		return clientCall;
	}

	/**
	 * Retrieves the standard (otherwise unused) port for broadcasting.
	 *
	 * @return
	 */
	public int getMulticastPort() {
		return 6112;
	}

	/**
	 * Returns the prefix for the datagram containing the server URL.
	 *
	 * @return
	 */
	public String getResponsePrefix() {
		return responsePrefix;
	}
}

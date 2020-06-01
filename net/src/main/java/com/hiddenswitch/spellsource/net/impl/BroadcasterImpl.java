package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.net.Broadcaster;
import com.hiddenswitch.spellsource.net.Gateway;
import com.hiddenswitch.spellsource.net.Configuration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BroadcasterImpl extends AbstractVerticle implements Broadcaster {
	private final Logger LOGGER = LoggerFactory.getLogger(Broadcaster.class);
	private final String multicastAddress = "230.0.0.1";
	private final String clientCall = "LOXX";
	private final String responsePrefix = "SPLL";
	private Map<NetworkInterface, DatagramSocket> hostSockets = new HashMap<>();

	@Override
	public void start(Promise<Void> startFuture) throws Exception {
		final NetworkInterface networkInterface = Gateway.mainInterface();
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
					next.result().listenMulticastGroup(multicastAddress, networkInterface.getName(), null, listener -> {
						if (listener.failed()) {
							LOGGER.error("createDatagramSocket: Failed to listen to multicast group", listener.cause());
							isListening.fail(listener.cause());
						}
						final DatagramSocket socket = listener.result();
						socket.handler(packet -> {
							if (!packet.data().getString(0, clientCall.length()).equals(clientCall)) {
								return;
							}

							LOGGER.info("createDatagramSocket: Replying to datagram received from " + packet.sender().toString());
							// Reply with the local base path
							String host = Gateway.getHostIpAddress();
							socket.send(getResponsePrefix() + "http://" + host + ":" + Configuration.apiGatewayPort() + "/", packet.sender().port(), packet.sender().host(), Promise.promise());
						});

						isListening.complete();
					});
				});
	}

	@Override
	public void stop(Promise<Void> stopFuture) throws Exception {
		CompositeFuture.join(hostSockets.values().stream().map(ds -> {
			Promise<Void> future = Promise.promise();
			ds.close(future);
			return future.future();
		}).collect(Collectors.toList())).setHandler(then -> stopFuture.complete());
	}

	@Override
	public String getMulticastAddress() {
		return multicastAddress;
	}

	@Override
	public String getClientCall() {
		return clientCall;
	}

	@Override
	public int getMulticastPort() {
		return 6112;
	}

	@Override
	public String getResponsePrefix() {
		return responsePrefix;
	}
}

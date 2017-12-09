package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Broadcaster;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Port;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BroadcasterImpl extends AbstractVerticle implements Broadcaster {
	private final String multicastAddress = "230.0.0.1";
	private final String clientCall = "LOXX";
	private final String responsePrefix = "SPLL";
	private Map<NetworkInterface, DatagramSocket> hostSockets = new HashMap<>();

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		final NetworkInterface networkInterface = Gateway.mainInterface();
		if (networkInterface == null) {
			startFuture.fail("No valid network interface found.");
			return;
		}
		hostSockets.put(networkInterface, createDatagramSocket(networkInterface, startFuture));
	}

	private DatagramSocket createDatagramSocket(final NetworkInterface networkInterface, Future<Void> isListening) throws SocketException {
		final String host = Gateway.getHostAddress();
		if (host == null) {
			isListening.fail("No valid IPv4 host address found.");
			return null;
		}

		return vertx.createDatagramSocket(new DatagramSocketOptions()
				.setReuseAddress(true)
				.setReusePort(true))
				.listen(getMulticastPort(), "0.0.0.0", next -> {
					next.result().listenMulticastGroup(multicastAddress, networkInterface.getName(), null, listener -> {
						final DatagramSocket socket = listener.result();
						socket.handler(packet -> {
							if (!packet.data().getString(0, clientCall.length()).equals(clientCall)) {
								return;
							}

							// Reply with the local base path
							socket.send(getResponsePrefix() + "http://" + host + ":" + Integer.toString(Port.port()), getMulticastPort(), getMulticastAddress(), Future.future());
						});

						isListening.complete();
					});
				});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		CompositeFuture.join(hostSockets.values().stream().map(ds -> {
			Future<Void> future = Future.future();
			ds.close(future);
			return future;
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

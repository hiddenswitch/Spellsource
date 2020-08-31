package com.hiddenswitch.spellsource.net.tests.impl;

import io.vertx.core.Handler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.netty.ws.NettyWebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.junit.jupiter.api.Assertions.fail;


public class NettyWebSocketClientEndpoint implements WebSocketListener, TestWebSocket, AutoCloseable {
	private static Logger LOGGER = LoggerFactory.getLogger(NettyWebSocketClientEndpoint.class);
	private final String endpoint;
	private final String auth;
	private NettyWebSocket websocket;
	private Handler<String> messageHandler;
	private Runnable closeHandler;
	private static final int webSocketMaxFrameSize = 65536;
	private static final AsyncHttpClient client = asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
			.setWebSocketMaxFrameSize(webSocketMaxFrameSize)
			.setHandshakeTimeout(3000)
			.setConnectTimeout(3000)
			.setMaxConnections(256));

	public NettyWebSocketClientEndpoint(String endpoint, String auth) {
		this.endpoint = endpoint;
		this.auth = auth;
	}

	public void connect() {
		int retries = 3;
		while (websocket == null && retries > 0) {
			CompletableFuture<NettyWebSocket> res = client.prepareGet(this.endpoint + "?X-Auth-Token=" + this.auth)
					.execute(new WebSocketUpgradeHandler.Builder()
							.addWebSocketListener(this).build()).toCompletableFuture();
			try {
				websocket = res.get();
				return;
			} catch (ExecutionException timeout) {
				// Retry connecting
				LOGGER.warn("connect: Retrying to connect {} more times", retries - 1);
			} catch (InterruptedException e) {
				return;
			}
			retries--;
		}
	}

	@Override
	public void onOpen(org.asynchttpclient.ws.WebSocket websocket) {
	}

	@Override
	public void onClose(org.asynchttpclient.ws.WebSocket websocket, int code, String reason) {
		if (closeHandler != null) {
			closeHandler.run();
		}
	}

	public void onError(Throwable e) {
		ExceptionUtils.printRootCauseStackTrace(e);
		fail(e.getMessage());
	}

	@Override
	public void onBinaryFrame(byte[] payload, boolean finalFragment, int rsv) {
		if (this.messageHandler != null) {
			this.messageHandler.handle(new String(payload, Charset.defaultCharset()));
		}
	}

	@Override
	public void onTextFrame(String payload, boolean finalFragment, int rsv) {
		if (this.messageHandler != null) {
			this.messageHandler.handle(payload);
		}
	}

	/**
	 * register message handler
	 *
	 * @param msgHandler
	 */
	@Override
	public void setMessageHandler(Handler<String> msgHandler) {
		this.messageHandler = msgHandler;
	}

	/**
	 * Send a message.
	 *
	 * @param message
	 */
	@Override
	public void sendMessage(String message) {
		boolean isFirst = true;
		try {
			while (message.length() > webSocketMaxFrameSize) {
				String substring = message.substring(0, webSocketMaxFrameSize);
				if (isFirst) {
					isFirst = false;
					websocket.sendTextFrame(substring, false, 0).await();
				} else {
					websocket.sendContinuationFrame(substring, false, 0).await();
				}
				message = message.substring(webSocketMaxFrameSize);
			}

			if (isFirst) {
				websocket.sendTextFrame(message).await();
			} else {
				websocket.sendContinuationFrame(message, true, 0).await();
			}
		} catch (InterruptedException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void close() {
		try {
			if (websocket != null) {
				websocket.sendCloseFrame();
			}
			if (closeHandler != null) {
				closeHandler.run();
			}
		} catch (Throwable any) {
		}
	}

	@Override
	public boolean isOpen() {
		return websocket != null && websocket.isOpen();
	}

	@Override
	public NettyWebSocketClientEndpoint setCloseHandler(Runnable closeHandler) {
		this.closeHandler = closeHandler;
		return this;
	}
}

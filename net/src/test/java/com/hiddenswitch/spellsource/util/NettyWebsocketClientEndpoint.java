package com.hiddenswitch.spellsource.util;

import io.vertx.core.Handler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.netty.ws.NettyWebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.junit.Assert;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;


public class NettyWebsocketClientEndpoint implements WebSocketListener, TestWebsocket {
	private NettyWebSocket websocket;
	private Handler<String> messageHandler;
	private Runnable closeHandler;
	private int webSocketMaxFrameSize = 65536;
	private final CompletableFuture<NettyWebSocket> nettyWebSocketCompletableFuture;

	public NettyWebsocketClientEndpoint(String endpoint, String auth) {
		try {
			AsyncHttpClient client = asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
					.setWebSocketMaxFrameSize(webSocketMaxFrameSize)
					.setMaxConnections(1024));
			nettyWebSocketCompletableFuture = client.prepareGet(endpoint + "?X-Auth-Token=" + auth)
					.execute(new WebSocketUpgradeHandler.Builder()
							.addWebSocketListener(this).build()).toCompletableFuture();
			websocket = nettyWebSocketCompletableFuture.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		Assert.fail(e.getMessage());
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
			websocket.sendCloseFrame();
			if (closeHandler != null) {
				closeHandler.run();
			}
		} catch (Throwable any) {
		}
	}

	@Override
	public boolean isOpen() {
		return websocket.isOpen();
	}

	@Override
	public NettyWebsocketClientEndpoint setCloseHandler(Runnable closeHandler) {
		this.closeHandler = closeHandler;
		return this;
	}

	public CompletableFuture<NettyWebSocket> getNettyWebSocketCompletableFuture() {
		return nettyWebSocketCompletableFuture;
	}
}

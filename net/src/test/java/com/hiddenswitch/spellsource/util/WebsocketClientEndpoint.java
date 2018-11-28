package com.hiddenswitch.spellsource.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.netty.ws.NettyWebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.junit.Assert;

import java.nio.charset.Charset;

import static org.asynchttpclient.Dsl.asyncHttpClient;


public class WebsocketClientEndpoint implements WebSocketListener {
	private final NettyWebSocket websocket;
	private MessageHandler messageHandler;
	private Runnable closeHandler;

	public WebsocketClientEndpoint(String endpoint, String auth) {
		try {
			AsyncHttpClient client = asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
					.setWebSocketMaxFrameSize(1024 * 1024)
					.setMaxConnections(1024));
			websocket = client.prepareGet(endpoint + "?X-Auth-Token=" + auth)
					.execute(new WebSocketUpgradeHandler.Builder()
							.addWebSocketListener(this).build()).get();
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
			this.messageHandler.handleMessage(new String(payload, Charset.defaultCharset()));
		}
	}

	@Override
	public void onTextFrame(String payload, boolean finalFragment, int rsv) {
		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(payload);
		}
	}

	/**
	 * register message handler
	 *
	 * @param msgHandler
	 */
	public void setMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	/**
	 * Send a message.
	 *
	 * @param message
	 */
	public void sendMessage(String message) {
		websocket.sendTextFrame(message);
	}

	public void close() {
		try {
			websocket.sendCloseFrame();
			if (closeHandler != null) {
				closeHandler.run();
			}
		} catch (Throwable any) {
		}
	}

	public boolean isOpen() {
		return websocket.isOpen();
	}

	@FunctionalInterface
	public interface MessageHandler {

		void handleMessage(String message);
	}

	public WebsocketClientEndpoint setCloseHandler(Runnable closeHandler) {
		this.closeHandler = closeHandler;
		return this;
	}
}

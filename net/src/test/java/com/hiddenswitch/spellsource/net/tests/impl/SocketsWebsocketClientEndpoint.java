package com.hiddenswitch.spellsource.net.tests.impl;

import com.hiddenswitch.spellsource.net.impl.SpellsourceAuthHandler;
import com.neovisionaries.ws.client.*;
import io.vertx.core.Handler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.nio.charset.Charset;


public class SocketsWebsocketClientEndpoint extends WebSocketAdapter implements TestWebsocket {
	private static Logger LOGGER = LoggerFactory.getLogger(SocketsWebsocketClientEndpoint.class);
	private static WebSocketFactory webSocketFactory = new WebSocketFactory()
			.setSocketFactory(SocketFactory.getDefault());
	private final WebSocket websocket;
	private Handler<String> messageHandler;
	private Runnable closeHandler;

	public SocketsWebsocketClientEndpoint(String endpoint, String auth) {
		try {
			String uri = endpoint + "?" + SpellsourceAuthHandler.HEADER + "=" + auth;
			websocket = webSocketFactory.createSocket(uri, 10000);
			websocket.setMaxPayloadSize(1024);
			websocket.addListener(this);
			websocket.connect();
			LOGGER.info("constructor {}: connected", uri);
		} catch (Exception e) {
			LOGGER.error("constructor:", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
		ExceptionUtils.printRootCauseStackTrace(exception);
		throw new AssertionError(exception);
	}

	@Override
	public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
		if (closeHandler != null) {
			closeHandler.run();
		}
	}

	@Override
	public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
		if (this.messageHandler != null) {
			this.messageHandler.handle(new String(binary, Charset.defaultCharset()));
		}
	}

	@Override
	public void onTextMessage(WebSocket websocket, String text) throws Exception {
		if (this.messageHandler != null) {
			this.messageHandler.handle(text);
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
	public void sendMessage(String message) {
		websocket.sendText(message);
	}

	public void close() {
		try {
			websocket.sendClose();
			if (closeHandler != null) {
				closeHandler.run();
			}
		} catch (Throwable any) {
			ExceptionUtils.printRootCauseStackTrace(any);
		}
	}

	public boolean isOpen() {
		return websocket.isOpen();
	}

	public SocketsWebsocketClientEndpoint setCloseHandler(Runnable closeHandler) {
		this.closeHandler = closeHandler;
		return this;
	}
}

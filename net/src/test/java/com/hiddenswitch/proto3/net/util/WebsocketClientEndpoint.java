package com.hiddenswitch.proto3.net.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;

import javax.websocket.*;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class WebsocketClientEndpoint {
	private Session userSession = null;
	private MessageHandler messageHandler;

	public WebsocketClientEndpoint(URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Callback hook for Connection open events.
	 *
	 * @param userSession the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(Session userSession) {
		this.userSession = userSession;
	}

	/**
	 * Callback hook for Connection close events.
	 *
	 * @param userSession the userSession which is getting closed.
	 * @param reason      the reason for connection close
	 */
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
	}

	/**
	 * Callback hook for Message Events. This method will be invoked when a client send a message.
	 *
	 * @param message The text message
	 */
	@OnMessage
	public void onMessage(String message) {
		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(message);
		}
	}

	@OnMessage
	public void onMessage(byte[] message) {
		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(new String(message, Charset.defaultCharset()));
		}
	}

	@OnError
	public void onError(Throwable e) {
		ExceptionUtils.printRootCauseStackTrace(e);
		Assert.fail(e.getMessage());
	}

	/**
	 * register message handler
	 *
	 * @param msgHandler
	 */
	public void addMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	/**
	 * Send a message.
	 *
	 * @param message
	 */
	public void sendMessage(String message) {
		this.userSession.getAsyncRemote().sendText(message);
	}

	public Session getUserSession() {
		return userSession;
	}

	@FunctionalInterface
	public interface MessageHandler {

		void handleMessage(String message);
	}
}

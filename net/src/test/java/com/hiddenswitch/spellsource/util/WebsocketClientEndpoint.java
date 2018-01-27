package com.hiddenswitch.spellsource.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.ThreadPoolConfig;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientProperties;
import org.junit.Assert;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

@ClientEndpoint
public class WebsocketClientEndpoint {
	private Session userSession = null;
	private MessageHandler messageHandler;

	public WebsocketClientEndpoint(String endpoint, String auth) {
		try {
			URI endpointURI = new URI(endpoint + "?X-Auth-Token=" + auth);
			ClientManager client = ClientManager.createClient();
			client.getProperties().put(ClientProperties.SHARED_CONTAINER, true);
			client.getProperties().put(GrizzlyClientProperties.SELECTOR_THREAD_POOL_CONFIG, ThreadPoolConfig.defaultConfig().setMaxPoolSize(256));
			client.getProperties().put(GrizzlyClientProperties.WORKER_THREAD_POOL_CONFIG, ThreadPoolConfig.defaultConfig().setMaxPoolSize(256));
			client.connectToServer(this, endpointURI);
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

	public void close() {
		try {
			userSession.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FunctionalInterface
	public interface MessageHandler {

		void handleMessage(String message);
	}
}

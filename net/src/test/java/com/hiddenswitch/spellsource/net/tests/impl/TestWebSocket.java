package com.hiddenswitch.spellsource.net.tests.impl;

import io.vertx.core.Handler;

public interface TestWebSocket {
	void setMessageHandler(Handler<String> msgHandler);

	void sendMessage(String message);

	void close();

	boolean isOpen();

	TestWebSocket setCloseHandler(Runnable closeHandler);
}

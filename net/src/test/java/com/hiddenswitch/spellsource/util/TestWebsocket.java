package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;

import java.util.function.Consumer;

public interface TestWebsocket {
	void setMessageHandler(Handler<String> msgHandler);

	void sendMessage(String message);

	void close();

	boolean isOpen();

	TestWebsocket setCloseHandler(Runnable closeHandler);
}

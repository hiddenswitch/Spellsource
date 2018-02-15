package com.hiddenswitch.spellsource.util;

import ch.qos.logback.classic.Level;
import io.netty.handler.codec.http.websocketx.WebSocket08FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket08FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.slf4j.LoggerFactory;

public class Logging {
	static {
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
	}

	public static void setLoggingLevelForClass(Class clazz, Level level) {
		ch.qos.logback.classic.Logger _logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(clazz);
		_logger.setLevel(level);
	}

	public static void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);

		setLoggingLevelForClass(WebSocket08FrameEncoder.class, Level.ERROR);
		setLoggingLevelForClass(WebSocket08FrameDecoder.class, Level.ERROR);
		setLoggingLevelForClass(WebSocketServerHandshaker.class, Level.ERROR);
	}

	public static void setLoggingLevel() {
		setLoggingLevel(Level.toLevel(System.getProperty("spellsource.root.logger.level"), Level.ERROR));
	}
}

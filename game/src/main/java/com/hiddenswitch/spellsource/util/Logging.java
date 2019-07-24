package com.hiddenswitch.spellsource.util;

import ch.qos.logback.classic.Level;
import io.netty.handler.codec.http.websocketx.WebSocket08FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket08FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for writing to logs, and suppresses some annoying logs from Netty classes.
 */
public class Logging {
	private static ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
			.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

	static {
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
	}

	/**
	 * Retrieves the root logger.
	 *
	 * @return
	 */
	public static Logger root() {
		return root;
	}

	/**
	 * Overrides the configured logging level for the specified class at runtime.
	 *
	 * @param clazz
	 * @param level
	 */
	public static void setLoggingLevelForClass(Class clazz, Level level) {
		ch.qos.logback.classic.Logger _logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(clazz);
		_logger.setLevel(level);
	}

	/**
	 * Sets the <b>global</b> logging level before any overrides. This is the "default" logging level.
	 *
	 * @param level
	 */
	public static void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
		setLoggingLevelForClass(WebSocket08FrameEncoder.class, Level.ERROR);
		setLoggingLevelForClass(WebSocket08FrameDecoder.class, Level.ERROR);
		setLoggingLevelForClass(WebSocketServerHandshaker.class, Level.ERROR);
	}
}

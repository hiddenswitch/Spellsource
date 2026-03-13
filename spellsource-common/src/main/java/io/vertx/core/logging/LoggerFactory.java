package io.vertx.core.logging;

/**
 * Shim for the removed {@code io.vertx.core.logging.LoggerFactory} class.
 * Bridges to SLF4J so that libraries compiled against Vert.x 4 logging still work at runtime.
 */
public class LoggerFactory {

	public static Logger getLogger(Class<?> clazz) {
		return new Logger(org.slf4j.LoggerFactory.getLogger(clazz));
	}

	public static Logger getLogger(String name) {
		return new Logger(org.slf4j.LoggerFactory.getLogger(name));
	}
}

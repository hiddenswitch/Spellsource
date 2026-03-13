package io.vertx.core.logging;

/**
 * Shim for the removed {@code io.vertx.core.logging.Logger} class.
 * Bridges to SLF4J so that libraries compiled against Vert.x 4 logging still work at runtime.
 */
public class Logger {

	private final org.slf4j.Logger delegate;

	Logger(org.slf4j.Logger delegate) {
		this.delegate = delegate;
	}

	public boolean isDebugEnabled() {
		return delegate.isDebugEnabled();
	}

	public void debug(Object message, Object... params) {
		if (delegate.isDebugEnabled()) {
			delegate.debug(String.valueOf(message), params);
		}
	}

	public void info(Object message, Object... params) {
		if (delegate.isInfoEnabled()) {
			delegate.info(String.valueOf(message), params);
		}
	}

	public void warn(Object message, Object... params) {
		if (delegate.isWarnEnabled()) {
			delegate.warn(String.valueOf(message), params);
		}
	}

	public void error(Object message, Object... params) {
		if (delegate.isErrorEnabled()) {
			delegate.error(String.valueOf(message), params);
		}
	}

	public void trace(Object message, Object... params) {
		if (delegate.isTraceEnabled()) {
			delegate.trace(String.valueOf(message), params);
		}
	}
}

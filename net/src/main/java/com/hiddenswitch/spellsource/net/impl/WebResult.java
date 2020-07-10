package com.hiddenswitch.spellsource.net.impl;

import io.vertx.core.AsyncResult;

/**
 * Standard responses for Spellsource REST services
 *
 * @param <T>
 */
public interface WebResult<T> extends AsyncResult<T> {
	default int responseCode() {
		if (cause() != null) {
			return 500;
		} else {
			return 200;
		}
	}

	static <T> WebResult<T> succeeded(T result) {
		return new WebResultImpl<T>(result);
	}

	static <T> WebResult<T> failed(Throwable cause) {
		return new WebResultImpl<T>(cause);
	}

	static <T> WebResult<T> succeeded(int responseCode, T result) {
		return new WebResultImpl<T>(responseCode, result);
	}

	static <T> WebResult<T> failed(int responseCode, Throwable cause) {
		return new WebResultImpl<T>(responseCode, cause);
	}

	static <T> WebResult<T> notFound(String message, Object... objs) {
		return WebResult.failed(404, new NullPointerException(String.format(message, objs)));
	}

	static <T> WebResult<T> forbidden(String message, Object... objs) {
		return WebResult.failed(403, new SecurityException(String.format(message, objs)));
	}

	static <T> WebResult<T> unsupported(String message, Object... objs) {
		return WebResult.failed(501, new UnsupportedOperationException(String.format(message, objs)));
	}

	static <T> WebResult<T> illegalState(String message, Object... objs) {
		return WebResult.failed(418, new IllegalStateException(String.format(message, objs)));
	}

	static <T> WebResult<T> invalidArgument(String message, Object... objs) {
		return WebResult.failed(400, new IllegalArgumentException(String.format(message, objs)));
	}
}


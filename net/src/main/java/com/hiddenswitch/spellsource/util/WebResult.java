package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.impl.WebResultImpl;
import io.vertx.core.AsyncResult;

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
}


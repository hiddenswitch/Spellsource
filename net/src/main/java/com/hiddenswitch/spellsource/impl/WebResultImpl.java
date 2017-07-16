package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.WebResult;

import java.io.Serializable;

/**
 * Created by bberman on 2/15/17.
 */
public class WebResultImpl<T> implements WebResult<T>, Serializable {
	private final T result;
	private final Throwable cause;
	private final Integer responseCode;

	public WebResultImpl(T result) {
		this.result = result;
		this.cause = null;
		this.responseCode = 200;
	}

	public WebResultImpl(Throwable cause) {
		this.result = null;
		this.cause = cause;
		this.responseCode = 500;
	}

	public WebResultImpl(int responseCode, T result) {
		this.result = result;
		this.cause = null;
		this.responseCode = responseCode;
	}

	public WebResultImpl(int responseCode, Throwable cause) {
		this.result = null;
		this.cause = cause;
		this.responseCode = responseCode;
	}

	@Override
	public T result() {
		return this.result;
	}

	@Override
	public Throwable cause() {
		return this.cause;
	}

	@Override
	public boolean succeeded() {
		return result != null;
	}

	@Override
	public boolean failed() {
		return cause != null;
	}

	@Override
	public int responseCode() {
		return responseCode;
	}

	@Override
	public String toString() {
		if (succeeded()) {
			return Serialization.serialize(result);
		} else {
			return Serialization.serialize(cause);
		}
	}
}

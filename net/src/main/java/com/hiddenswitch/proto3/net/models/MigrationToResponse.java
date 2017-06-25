package com.hiddenswitch.proto3.net.models;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.io.Serializable;

public class MigrationToResponse implements Serializable {
	private boolean failed;
	private boolean succeeded;
	private Handler<AsyncResult<Void>> handler;
	private Void result;
	private Throwable throwable;

	/**
	 * The result of the operation. This will be null if the operation failed.
	 */
	public Void result() {
		return result;
	}

	/**
	 * An exception describing failure. This will be null if the operation succeeded.
	 */
	public Throwable cause() {
		return throwable;
	}

	/**
	 * Did it succeeed?
	 */
	public boolean succeeded() {
		return succeeded;
	}

	/**
	 * Did it fail?
	 */
	public boolean failed() {
		return failed;
	}

	/**
	 * Has it completed?
	 */
	public boolean isComplete() {
		return failed || succeeded;
	}

	public static MigrationToResponse failedMigration() {
		MigrationToResponse r = new MigrationToResponse();
		r.failed = true;
		r.succeeded = false;
		return r;
	}

	public static MigrationToResponse succeededMigration() {
		MigrationToResponse r = new MigrationToResponse();
		r.failed = false;
		r.succeeded = true;
		return r;
	}

	public static MigrationToResponse failedMigration(Throwable e) {
		MigrationToResponse r = new MigrationToResponse();
		r.failed = true;
		r.succeeded = false;
		r.throwable = e;
		return r;
	}
}

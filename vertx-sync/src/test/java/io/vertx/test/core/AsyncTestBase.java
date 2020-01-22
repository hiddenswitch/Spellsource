/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.test.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class AsyncTestBase {

	private static final Logger log = LoggerFactory.getLogger(AsyncTestBase.class);

	private CountDownLatch latch;
	private volatile Throwable throwable;
	private volatile Thread thrownThread;
	private volatile boolean testCompleteCalled;
	private volatile boolean awaitCalled;
	private boolean threadChecksEnabled = true;
	private volatile boolean tearingDown;
	private volatile String mainThreadName;
	private Map<String, Exception> threadNames = new ConcurrentHashMap<>();


	protected void setUp(TestInfo testInfo) throws Exception {
		log.info("Starting test: " + this.getClass().getSimpleName() + "#" + testInfo.getTestMethod().get().getName());
		mainThreadName = Thread.currentThread().getName();
		tearingDown = false;
		waitFor(1);
		throwable = null;
		testCompleteCalled = false;
		awaitCalled = false;
		threadNames.clear();
	}

	protected void tearDown() throws Exception {
		tearingDown = true;
		afterAsyncTestBase();
	}

	@BeforeEach
	public void before(TestInfo testInfo) throws Exception {
		setUp(testInfo);
	}

	@AfterEach
	public void after() throws Exception {
		tearDown();
	}

	protected synchronized void waitFor(int count) {
		latch = new CountDownLatch(count);
	}

	protected synchronized void waitForMore(int count) {
		latch = new CountDownLatch(count + (int) latch.getCount());
	}

	protected synchronized void complete() {
		if (tearingDown) {
			throw new IllegalStateException("testComplete called after test has completed");
		}
		checkThread();
		if (testCompleteCalled) {
			throw new IllegalStateException("already complete");
		}
		latch.countDown();
		if (latch.getCount() == 0) {
			testCompleteCalled = true;
		}
	}

	protected void testComplete() {
		if (tearingDown) {
			throw new IllegalStateException("testComplete called after test has completed");
		}
		checkThread();
		if (testCompleteCalled) {
			throw new IllegalStateException("testComplete() already called");
		}
		testCompleteCalled = true;
		latch.countDown();
	}

	protected void await() {
		await(2, TimeUnit.MINUTES);
	}

	public void await(long delay, TimeUnit timeUnit) {
		if (awaitCalled) {
			throw new IllegalStateException("await() already called");
		}
		awaitCalled = true;
		try {
			boolean ok = latch.await(delay, timeUnit);
			if (!ok) {
				// timed out
				throw new IllegalStateException("Timed out in waiting for test complete");
			} else {
				rethrowError();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException("Test thread was interrupted!");
		}
	}

	private void rethrowError() {
		if (throwable != null) {
			if (throwable instanceof Error) {
				throw (Error) throwable;
			} else if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			} else {
				// Unexpected throwable- Should never happen
				throw new IllegalStateException(throwable);
			}

		}
	}

	protected void disableThreadChecks() {
		threadChecksEnabled = false;
	}

	protected void afterAsyncTestBase() {
		if (throwable != null && thrownThread != Thread.currentThread() && !awaitCalled) {
			// Throwable caught from non main thread
			throw new IllegalStateException("Assert or failure from non main thread but no await() on main thread", throwable);
		}
		for (Map.Entry<String, Exception> entry : threadNames.entrySet()) {
			if (!entry.getKey().equals(mainThreadName)) {
				if (threadChecksEnabled && !entry.getKey().startsWith("vert.x-")) {
					IllegalStateException is = new IllegalStateException("Non Vert.x thread! :" + entry.getKey());
					is.setStackTrace(entry.getValue().getStackTrace());
					throw is;
				}
			}
		}

	}

	private void handleThrowable(Throwable t) {
		if (tearingDown) {
			throw new IllegalStateException("assert or failure occurred after test has completed");
		}
		throwable = t;
		t.printStackTrace();
		thrownThread = Thread.currentThread();
		latch.countDown();
		if (t instanceof AssertionError) {
			throw (AssertionError) t;
		}
	}

	protected void clearThrown() {
		throwable = null;
	}

	protected void checkThread() {
		threadNames.put(Thread.currentThread().getName(), new Exception());
	}

	protected void assertTrue(String message, boolean condition) {
		checkThread();
		try {
			Assertions.assertTrue(condition,message);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}

	protected void assertFalse(boolean condition) {
		checkThread();
		try {
			Assertions.assertFalse(condition);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}

	protected void fail(String message) {
		checkThread();
		try {
			Assertions.fail(message);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}


	protected void assertTrue(boolean condition) {
		checkThread();
		try {
			Assertions.assertTrue(condition);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}

	@Deprecated
	protected void assertEquals(String message, Object[] expecteds, Object[] actuals) {
		checkThread();
		try {
			Assertions.assertEquals(expecteds, actuals, message);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}

	@Deprecated
	protected void assertEquals(Object[] expecteds, Object[] actuals) {
		checkThread();
		try {
			Assertions.assertEquals(expecteds, actuals);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}

	@Deprecated
	protected void assertEquals(double expected, double actual) {
		checkThread();
		try {
			Assertions.assertEquals(expected, actual);
		} catch (AssertionError e) {
			handleThrowable(e);
		}
	}

	protected <T> Handler<AsyncResult<T>> onFailure(Consumer<Throwable> consumer) {
		return result -> {
			assertFalse(result.succeeded());
			consumer.accept(result.cause());
		};
	}

	protected void awaitLatch(CountDownLatch latch) throws InterruptedException {
		assertTrue(latch.await(10, TimeUnit.SECONDS));
	}

	public static void assertWaitUntil(BooleanSupplier supplier) {
		assertWaitUntil(supplier, 10000);
	}

	public static void waitUntil(BooleanSupplier supplier) {
		waitUntil(supplier, 10000);
	}

	public static <T> void waitUntilEquals(T value, Supplier<T> supplier) {
		waitUntil(() -> Objects.equals(value, supplier.get()), 10000);
	}

	public static void assertWaitUntil(BooleanSupplier supplier, long timeout) {
		if (!waitUntil(supplier, timeout)) {
			throw new IllegalStateException("Timed out");
		}
	}

	public static void assertWaitUntil(BooleanSupplier supplier, long timeout, String reason) {
		if (!waitUntil(supplier, timeout)) {
			throw new IllegalStateException("Timed out: " + reason);
		}
	}

	public static boolean waitUntil(BooleanSupplier supplier, long timeout) {
		long start = System.currentTimeMillis();
		while (true) {
			if (supplier.getAsBoolean()) {
				return true;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignore) {
			}
			long now = System.currentTimeMillis();
			if (now - start > timeout) {
				return false;
			}
		}
	}

	protected <T> Handler<AsyncResult<T>> onSuccess(Consumer<T> consumer) {
		return result -> {
			if (result.failed()) {
				result.cause().printStackTrace();
				fail(result.cause().getMessage());
			} else {
				consumer.accept(result.result());
			}
		};
	}

	protected void close(Vertx vertx) throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		vertx.close(ar -> {
			latch.countDown();
		});
		awaitLatch(latch);
	}
}

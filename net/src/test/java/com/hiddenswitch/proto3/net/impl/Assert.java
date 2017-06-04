package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * Created by bberman on 6/4/17.
 */
class Assert implements TestContext {
	@Override
	@Suspendable
	public <T> T get(String key) {
		return ServiceTest.wrappedContext.get(key);
	}

	@Override
	@Suspendable
	public <T> T put(String key, Object value) {
		return ServiceTest.wrappedContext.put(key, value);
	}

	@Override
	@Suspendable
	public <T> T remove(String key) {
		return ServiceTest.wrappedContext.remove(key);
	}

	@Override
	@Suspendable
	public TestContext assertNull(Object expected) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertNull(expected);
		} else {
			ServiceTest.wrappedContext.assertNull(expected);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertNull(Object expected, String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertNull(message, expected);
		} else {
			ServiceTest.wrappedContext.assertNull(expected, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertNotNull(Object expected) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertNotNull(expected);
		} else {
			ServiceTest.wrappedContext.assertNotNull(expected);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertNotNull(Object expected, String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertNotNull(message, expected);
		} else {
			ServiceTest.wrappedContext.assertNotNull(expected, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertTrue(boolean condition) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertTrue(condition);
		} else {
			ServiceTest.wrappedContext.assertTrue(condition);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertTrue(boolean condition, String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertTrue(message, condition);
		} else {
			ServiceTest.wrappedContext.assertTrue(condition, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertFalse(boolean condition) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertFalse(condition);
		} else {
			ServiceTest.wrappedContext.assertFalse(condition);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertFalse(boolean condition, String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertFalse(message, condition);
		} else {
			ServiceTest.wrappedContext.assertFalse(condition, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertEquals(Object expected, Object actual) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertEquals(expected, actual);
		} else {
			ServiceTest.wrappedContext.assertEquals(expected, actual);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertEquals(Object expected, Object actual, String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertEquals(message, expected, actual);
		} else {
			ServiceTest.wrappedContext.assertEquals(expected, actual, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertInRange(double expected, double actual, double delta) {
		if (ServiceTest.wrappedContext == null) {
			throw new RuntimeException("Unsupported!");
		} else {
			ServiceTest.wrappedContext.assertInRange(expected, actual, delta);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertInRange(double expected, double actual, double delta, String message) {
		if (ServiceTest.wrappedContext == null) {
			throw new RuntimeException("Unsupported!");
		} else {
			ServiceTest.wrappedContext.assertInRange(expected, actual, delta, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertNotEquals(Object first, Object second) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertNotEquals(first, second);
		} else {
			ServiceTest.wrappedContext.assertNotEquals(first, second);
		}
		return this;
	}

	@Override
	@Suspendable
	public TestContext assertNotEquals(Object first, Object second, String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.assertNotEquals(message, first, second);
		} else {
			ServiceTest.wrappedContext.assertNotEquals(first, second, message);
		}
		return this;
	}

	@Override
	@Suspendable
	public void fail() {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.fail("(No message)");
		} else {
			ServiceTest.wrappedContext.fail();
		}
	}

	@Override
	@Suspendable
	public void fail(String message) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.fail(message);
		} else {
			ServiceTest.wrappedContext.fail(message);
		}
	}

	@Override
	@Suspendable
	public void fail(Throwable cause) {
		if (ServiceTest.wrappedContext == null) {
			org.junit.Assert.fail(cause.getMessage());
		} else {
			ServiceTest.wrappedContext.fail(cause);
		}
	}

	@Override
	public Async async() {
		return ServiceTest.wrappedContext.async();
	}

	@Override
	public Async async(int count) {
		return ServiceTest.wrappedContext.async(count);
	}

	@Override
	public <T> Handler<AsyncResult<T>> asyncAssertSuccess() {
		return ServiceTest.wrappedContext.asyncAssertSuccess();
	}

	@Override
	public <T> Handler<AsyncResult<T>> asyncAssertSuccess(Handler<T> resultHandler) {
		return ServiceTest.wrappedContext.asyncAssertSuccess(resultHandler);
	}

	@Override
	public <T> Handler<AsyncResult<T>> asyncAssertFailure() {
		return ServiceTest.wrappedContext.asyncAssertFailure();
	}

	@Override
	public <T> Handler<AsyncResult<T>> asyncAssertFailure(Handler<Throwable> causeHandler) {
		return ServiceTest.wrappedContext.asyncAssertFailure(causeHandler);
	}

	@Override
	public Handler<Throwable> exceptionHandler() {
		return ServiceTest.wrappedContext.exceptionHandler();
	}
}

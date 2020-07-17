/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.hiddenswitch.spellsource.util;

import java.util.function.BiConsumer;
import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Vert.x future utilities.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class VertxFutures {

	private VertxFutures() {
	}

	/**
	 * Wraps a void Vert.x handler.
	 */
	public static <T> BiConsumer<T, Throwable> voidHandler(Handler<AsyncResult<Void>> handler, Context context) {
		checkNotNull(handler, "handler cannot be null");
		checkNotNull(context, "context cannot be null");
		return (result, error) -> {
			if (error == null) {
				context.runOnContext(v -> Future.<Void>succeededFuture().setHandler(handler));
			} else {
				context.runOnContext(v -> Future.<Void>failedFuture(error).setHandler(handler));
			}
		};
	}

	/**
	 * Wraps a Vert.x handler.
	 */
	public static <T> BiConsumer<T, Throwable> resultHandler(Handler<AsyncResult<T>> handler, Context context) {
		checkNotNull(handler, "handler cannot be null");
		checkNotNull(context, "context cannot be null");
		return (result, error) -> {
			if (error == null) {
				context.runOnContext(v -> Future.succeededFuture(result).setHandler(handler));
			} else {
				context.runOnContext(v -> Future.<T>failedFuture(error).setHandler(handler));
			}
		};
	}

	/**
	 * Converts a return value.
	 */
	public static <T, U> BiConsumer<T, Throwable> convertHandler(Handler<AsyncResult<U>> handler, Function<T, U> converter, Context context) {
		return (result, error) -> {
			if (error == null) {
				context.runOnContext(v -> Future.succeededFuture(converter.apply(result)).setHandler(handler));
			} else {
				context.runOnContext(v -> Future.<U>failedFuture(error).setHandler(handler));
			}
		};
	}
}

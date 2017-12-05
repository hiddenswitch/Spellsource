package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.WebResult;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bberman on 2/17/17.
 */
public class HandlerFactory {
	static Logger logger = LoggerFactory.getLogger(HandlerFactory.class);

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, AuthorizedRequestHandler<T, R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			String userId = context.user().principal().getString("_id");
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			WebResult<R> result;
			try {
				result = internalHandler.call(context, userId, request);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, RequestHandler<T, R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			WebResult<R> result;
			try {
				result = internalHandler.call(context, request);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(String paramName, AuthorizedParamHandler<R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			String request = context.pathParam(paramName);
			String userId = context.user().principal().getString("_id");
			WebResult<R> result;
			try {
				result = internalHandler.call(context, userId, request);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, String paramName, AuthorizedBodyAndParamHandler<T, R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			String param = context.pathParam(paramName);
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			String userId = context.user().principal().getString("_id");
			WebResult<R> result;
			try {
				result = internalHandler.call(context, userId, param, request);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(AuthorizedHandler<R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			String userId = context.user().principal().getString("_id");
			WebResult<R> result;
			try {
				result = internalHandler.call(context, userId);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(EmptyHandler<R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			WebResult<R> result;
			try {
				result = internalHandler.call(context);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	private static <R> void respond(RoutingContext context, WebResult<R> result) {
		context.response().setStatusCode(result.responseCode());
		if (result.succeeded()) {
			if (result.result() == null) {
				// Allow empty response bodies
				context.response().end();
			} else {
				context.response().end(Serialization.serialize(result.result()));
			}
		} else {
			if (result.cause() != null) {
				context.fail(result.cause());
			} else {
				context.fail(result.responseCode());
			}
		}
	}

	public static <R> Handler<RoutingContext> paramHandler(String paramName, ParamHandler<R> internalHandler) {
		return Sync.suspendableHandler((context) -> {
			String request = context.pathParam(paramName);
			WebResult<R> result;
			try {
				result = internalHandler.call(context, request);
			} catch (RuntimeException ex) {
				result = WebResult.failed(ex);
			}
			respond(context, result);
		});
	}

	@FunctionalInterface
	public interface AuthorizedHandler<R> {
		WebResult<R> call(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;
	}

	@FunctionalInterface
	public interface AuthorizedRequestHandler<T, R> {
		WebResult<R> call(RoutingContext context, String userId, T request) throws SuspendExecution, InterruptedException;
	}

	@FunctionalInterface
	public interface RequestHandler<T, R> {
		WebResult<R> call(RoutingContext context, T request) throws SuspendExecution, InterruptedException;
	}

	@FunctionalInterface
	public interface AuthorizedParamHandler<R> {
		WebResult<R> call(RoutingContext context, String userId, String param) throws SuspendExecution, InterruptedException;
	}

	@FunctionalInterface
	public interface AuthorizedBodyAndParamHandler<T, R> {
		WebResult<R> call(RoutingContext context, String userId, String param, T request) throws SuspendExecution, InterruptedException;
	}

	@FunctionalInterface
	public interface ParamHandler<R> {
		WebResult<R> call(RoutingContext context, String param) throws SuspendExecution, InterruptedException;
	}

	@FunctionalInterface
	public interface EmptyHandler<R> {
		WebResult<R> call(RoutingContext context) throws SuspendExecution, InterruptedException;
	}
}

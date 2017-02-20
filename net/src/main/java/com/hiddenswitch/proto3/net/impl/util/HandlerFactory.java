package com.hiddenswitch.proto3.net.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.proto3.net.util.Serialization;
import com.hiddenswitch.proto3.net.util.WebResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hiddenswitch.proto3.net.util.Sync.suspendableHandler;

/**
 * Created by bberman on 2/17/17.
 */
public class HandlerFactory {
	static Logger logger = LoggerFactory.getLogger(HandlerFactory.class);
	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, AuthorizedRequestHandler<T, R> internalHandler) {
		return suspendableHandler((context) -> {
			String userId = context.user().principal().getString("_id");
			T request = Json.decodeValue(context.getBodyAsString(), classT);
			WebResult<R> result = internalHandler.call(context, userId, request);
			respond(context, result);
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, RequestHandler<T, R> internalHandler) {
		return suspendableHandler((context) -> {
			T request = Json.decodeValue(context.getBodyAsString(), classT);
			WebResult<R> result = internalHandler.call(context, request);
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(String paramName, AuthorizedParamHandler<R> internalHandler) {
		return suspendableHandler((context) -> {
			String request = context.pathParam(paramName);
			String userId = context.user().principal().getString("_id");
			WebResult<R> result = internalHandler.call(context, userId, request);
			respond(context, result);
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, String paramName, AuthorizedBodyAndParamHandler<T, R> internalHandler) {
		return suspendableHandler((context) -> {
			String param = context.pathParam(paramName);
			T request = Json.decodeValue(context.getBodyAsString(), classT);
			String userId = context.user().principal().getString("_id");
			WebResult<R> result = internalHandler.call(context, userId, param, request);
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(AuthorizedHandler<R> internalHandler) {
		return suspendableHandler((context) -> {
			String userId = context.user().principal().getString("_id");
			WebResult<R> result = internalHandler.call(context, userId);
			respond(context, result);
		});
	}

	private static <R> void respond(RoutingContext context, WebResult<R> result) {
		if (result.succeeded()) {
			context.response().setStatusCode(result.responseCode());
			context.response().end(Serialization.serialize(result.result()));
		} else {
			context.fail(result.responseCode());
		}
	}

	public static <R> Handler<RoutingContext> paramHandler(String paramName, ParamHandler<R> internalHandler) {
		return suspendableHandler((context) -> {
			String request = context.pathParam(paramName);
			WebResult<R> result = internalHandler.call(context, request);
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
}

package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.Tracing;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.WebResult;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * Created by bberman on 2/17/17.
 */
public class HandlerFactory {
	static Logger logger = LoggerFactory.getLogger(HandlerFactory.class);

	private static Handler<RoutingContext> returnUnhandledExceptions(SuspendableAction1<RoutingContext> handler) {
		return suspendableHandler((context) -> {
			Throwable t = null;
			try {
				handler.call(context);
			} catch (NullPointerException notFound) {
				t = notFound;
				respond(context, WebResult.notFound("Not found (%s)", notFound.getMessage()));
			} catch (SecurityException notAuthorized) {
				t = notAuthorized;
				respond(context, WebResult.forbidden("Forbidden (%s)", notAuthorized.getMessage()));
			} catch (IllegalStateException illegalState) {
				t = illegalState;
				respond(context, WebResult.illegalState("Illegal state (%s)", illegalState.getMessage()));
			} catch (IllegalArgumentException illegalArgument) {
				t = illegalArgument;
				respond(context, WebResult.invalidArgument("Illegal argument (%s)", illegalArgument.getMessage()));
			} catch (Throwable unhandled) {
				t = unhandled;
				respond(context, WebResult.failed(500, unhandled));
			} finally {
				if (t != null) {
					Tracing.error(t);
					logger.error("handler error", t);
				}
			}
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, AuthorizedRequestHandler<T, R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String userId = Accounts.userId(context);
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			WebResult<R> result = internalHandler.call(context, userId, request);
			respond(context, result);
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, RequestHandler<T, R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			WebResult<R> result = internalHandler.call(context, request);
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(String paramName, AuthorizedParamHandler<R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String request = context.pathParam(paramName);
			String userId = Accounts.userId(context);
			WebResult<R> result = internalHandler.call(context, userId, request);
			respond(context, result);
		});
	}

	public static <T, R> Handler<RoutingContext> handler(Class<T> classT, String paramName, AuthorizedBodyAndParamHandler<T, R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String param = context.pathParam(paramName);
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			String userId = Accounts.userId(context);
			WebResult<R> result = internalHandler.call(context, userId, param, request);
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(AuthorizedHandler<R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String userId = Accounts.userId(context);
			WebResult<R> result = internalHandler.call(context, userId);
			respond(context, result);
		});
	}

	public static <R> Handler<RoutingContext> handler(EmptyHandler<R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			WebResult<R> result = internalHandler.call(context);
			respond(context, result);
		});
	}

	private static <R> void respond(RoutingContext context, WebResult<R> result) {
		context.response().setStatusCode(result.responseCode());
		if (context.response().closed()) {
			context.fail(new IOException("Response was closed (client disconnected)"));
			return;
		}

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
		return returnUnhandledExceptions((context) -> {
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

	@FunctionalInterface
	public interface EmptyHandler<R> {
		WebResult<R> call(RoutingContext context) throws SuspendExecution, InterruptedException;
	}
}

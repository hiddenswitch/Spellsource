package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.Gateway;
import com.hiddenswitch.spellsource.net.impl.Sync;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.net.impl.WebResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;

/**
 * Configures handlers for HTTP requests.
 * <p>
 * These handlers correctly configure the fiber (thread-like adaptor for async frameworks like vertx) that the HTTP
 * handler will run on.
 * <p>
 * Handlers can accept a combination of parameters: a user ID if the HTTP request requires authentication, some
 * parameters in the URL, and a JSON body deserialized to a specified type. This is determined by the signature of the
 * handler.
 *
 * @see Gateway for a full description of how to create new API methods.
 */
public interface Handlers {
	Logger LOGGER = LoggerFactory.getLogger(Handlers.class);

	static Handler<RoutingContext> returnUnhandledExceptions(SuspendableAction1<RoutingContext> handler) {
		return Sync.fiber((context) -> {
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
					LOGGER.error("handler error", t);
				}
			}
		});
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context, String userId, T request)}
	 * signature.
	 *
	 * @param classT          The request class.
	 * @param internalHandler The actual method.
	 * @param <T>             The request class type.
	 * @param <R>             The response class type.
	 * @return A new suspendable handler.
	 */
	static <T, R> Handler<RoutingContext> handler(Class<T> classT, AuthorizedRequestHandler<T, R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String userId = Accounts.userId(context);
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			WebResult<R> result = internalHandler.call(context, userId, request);
			respond(context, result);
		});
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context, T request)} signature.
	 *
	 * @param classT          The request class.
	 * @param internalHandler The actual method.
	 * @param <T>             The request class type.
	 * @param <R>             The response class type.
	 * @return A new suspendable handler.
	 */
	static <T, R> Handler<RoutingContext> handler(Class<T> classT, RequestHandler<T, R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			WebResult<R> result = internalHandler.call(context, request);
			respond(context, result);
		});
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context, String userId, String
	 * param)} signature.
	 *
	 * @param paramName       The name of the URL parameter (e.g., {@code "id"} for a URL like {@code "/docs/:id"}).
	 * @param internalHandler The actual method.
	 * @param <R>             The response class type
	 * @return A new suspendable handler.
	 */
	static <R> Handler<RoutingContext> handler(String paramName, AuthorizedParamHandler<R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String request = context.pathParam(paramName);
			String userId = Accounts.userId(context);
			WebResult<R> result = internalHandler.call(context, userId, request);
			respond(context, result);
		});
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context, String userId, String
	 * param, T request)} signature.
	 *
	 * @param classT          The request class.
	 * @param paramName       The name of the URL parameter (e.g., {@code "id"} for a URL like {@code "/docs/:id"}).
	 * @param internalHandler The actual method.
	 * @param <T>             The request class type.
	 * @param <R>             The response class type.
	 * @return A new suspendable handler.
	 */
	static <T, R> Handler<RoutingContext> handler(Class<T> classT, String paramName, AuthorizedBodyAndParamHandler<T, R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String param = context.pathParam(paramName);
			T request = Serialization.deserialize(context.getBodyAsString(), classT);
			String userId = Accounts.userId(context);
			WebResult<R> result = internalHandler.call(context, userId, param, request);
			respond(context, result);
		});
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context, String userId)}
	 * signature.
	 *
	 * @param internalHandler The actual method.
	 * @param <R>             The response class type.
	 * @return A new suspendable handler.
	 */
	static <R> Handler<RoutingContext> handler(AuthorizedHandler<R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String userId = Accounts.userId(context);
			WebResult<R> result = internalHandler.call(context, userId);
			respond(context, result);
		});
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context)} signature.
	 *
	 * @param internalHandler The actual method.
	 * @param <R>             The response class type.
	 * @return A new suspendable handler.
	 */
	static <R> Handler<RoutingContext> handler(EmptyHandler<R> internalHandler) {
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
			// Failure may have been set elsewhere by this point
		} else if (!context.failed()) {
			if (result.cause() != null) {
				context.fail(result.cause());
			} else {
				context.fail(result.responseCode());
			}
		}
	}

	/**
	 * Creates a handler for a method with the {@code WebResult<R> call(RoutingContext context, String param)} signature.
	 *
	 * @param paramName       The name of the URL parameter (e.g., {@code "id"} for a URL like {@code "/docs/:id"}).
	 * @param internalHandler The actual method.
	 * @param <R>             The response class type.
	 * @return A new suspendable handler.
	 */
	static <R> Handler<RoutingContext> paramHandler(String paramName, ParamHandler<R> internalHandler) {
		return returnUnhandledExceptions((context) -> {
			String request = context.pathParam(paramName);
			WebResult<R> result = internalHandler.call(context, request);
			respond(context, result);
		});
	}

	/**
	 * A method that requires authentication and returns data.
	 *
	 * @param <R> The response type
	 */
	@FunctionalInterface
	interface AuthorizedHandler<R> {
		WebResult<R> call(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;
	}

	/**
	 * A method that requires authentication, accepts a request and returns data.
	 *
	 * @param <T> The request type
	 * @param <R> The response type
	 */
	@FunctionalInterface
	interface AuthorizedRequestHandler<T, R> {
		WebResult<R> call(RoutingContext context, String userId, T request) throws SuspendExecution, InterruptedException;
	}

	/**
	 * A method that does not require authentication, accepts a request and returns data.
	 *
	 * @param <T> The request type
	 * @param <R> The response type.
	 */
	@FunctionalInterface
	interface RequestHandler<T, R> {
		WebResult<R> call(RoutingContext context, T request) throws SuspendExecution, InterruptedException;
	}

	/**
	 * A method that requires authentication, takes a URL parameter and returns data.
	 *
	 * @param <R> The response type
	 */
	@FunctionalInterface
	interface AuthorizedParamHandler<R> {
		WebResult<R> call(RoutingContext context, String userId, String param) throws SuspendExecution, InterruptedException;
	}

	/**
	 * A method that requires authentication, takes a URL parameter, accepts a request, and returns data.
	 *
	 * @param <T> The request type
	 * @param <R> The response type
	 */
	@FunctionalInterface
	interface AuthorizedBodyAndParamHandler<T, R> {
		WebResult<R> call(RoutingContext context, String userId, String param, T request) throws SuspendExecution, InterruptedException;
	}

	/**
	 * A method that does not require authentication, takes a URL parameter and returns data.
	 *
	 * @param <R> The response type.
	 */
	@FunctionalInterface
	interface ParamHandler<R> {
		WebResult<R> call(RoutingContext context, String param) throws SuspendExecution, InterruptedException;
	}

	/**
	 * A method that takes no arguments and returns data.
	 *
	 * @param <R> The response type
	 */
	@FunctionalInterface
	interface EmptyHandler<R> {
		WebResult<R> call(RoutingContext context) throws SuspendExecution, InterruptedException;
	}
}

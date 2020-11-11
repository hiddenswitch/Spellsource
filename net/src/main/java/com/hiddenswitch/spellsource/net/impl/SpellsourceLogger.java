package com.hiddenswitch.spellsource.net.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.impl.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Acts like a {@link io.vertx.ext.web.handler.impl.LoggerHandlerImpl} but censors out the auth token.
 */
public class SpellsourceLogger implements LoggerHandler {

	private final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * log before request or after
	 */
	private final boolean immediate;

	/**
	 * the current choosen format
	 */
	private final LoggerFormat format;

	private Pattern pattern = Pattern.compile(SpellsourceAuthHandler.HEADER + "=[^&]+&?");
	private Function<HttpServerRequest, String> customFormatter;

	public SpellsourceLogger(boolean immediate, LoggerFormat format) {
		this.immediate = immediate;
		this.format = format;
	}

	public SpellsourceLogger(LoggerFormat format) {
		this(false, format);
	}

	private String getClientAddress(SocketAddress inetSocketAddress) {
		if (inetSocketAddress == null) {
			return null;
		}
		return inetSocketAddress.host();
	}

	private void log(RoutingContext context, long timestamp, String remoteClient, HttpVersion version, HttpMethod method, String uri) {
		HttpServerRequest request = context.request();
		long contentLength = 0;
		if (immediate) {
			Object obj = request.headers().get("content-length");
			if (obj != null) {
				try {
					contentLength = Long.parseLong(obj.toString());
				} catch (NumberFormatException e) {
					// ignore it and continue
					contentLength = 0;
				}
			}
		} else {
			contentLength = request.response().bytesWritten();
		}
		String versionFormatted = "-";
		switch (version) {
			case HTTP_1_0:
				versionFormatted = "HTTP/1.0";
				break;
			case HTTP_1_1:
				versionFormatted = "HTTP/1.1";
				break;
			case HTTP_2:
				versionFormatted = "HTTP/2.0";
				break;
		}

		final MultiMap headers = request.headers();
		int status = request.response().getStatusCode();
		String message = null;

		// Remove X-Auth-Token
		uri = pattern.matcher(uri).replaceAll("");

		switch (format) {
			case DEFAULT:
				// as per RFC1945 the header is referer but it is not mandatory some implementations use referrer
				String referrer = headers.contains("referrer") ? headers.get("referrer") : headers.get("referer");
				String userAgent = request.headers().get("user-agent");
				referrer = referrer == null ? "-" : referrer;
				userAgent = userAgent == null ? "-" : userAgent;

				message = String.format("%s - - [%s] \"%s %s %s\" %d %d \"%s\" \"%s\"",
						remoteClient,
						Utils.formatRFC1123DateTime(timestamp),
						method,
						uri,
						versionFormatted,
						status,
						contentLength,
						referrer,
						userAgent);
				break;
			case SHORT:
				message = String.format("%s - %s %s %s %d %d - %d ms",
						remoteClient,
						method,
						uri,
						versionFormatted,
						status,
						contentLength,
						(System.currentTimeMillis() - timestamp));
				break;
			case TINY:
				message = String.format("%s %s %d %d - %d ms",
						method,
						uri,
						status,
						contentLength,
						(System.currentTimeMillis() - timestamp));
				break;
		}
		doLog(status, message);
	}

	protected void doLog(int status, String message) {
		if (status >= 500) {
			logger.error(message);
		} else if (status >= 400) {
			logger.warn(message);
		} else {
			logger.info(message);
		}
	}

	@Override
	public void handle(RoutingContext context) {
		// common logging data
		long timestamp = System.currentTimeMillis();
		String remoteClient = getClientAddress(context.request().remoteAddress());
		HttpMethod method = context.request().method();
		String uri = context.request().uri();
		HttpVersion version = context.request().version();

		if (immediate) {
			log(context, timestamp, remoteClient, version, method, uri);
		} else {
			context.addBodyEndHandler(v -> log(context, timestamp, remoteClient, version, method, uri));
		}

		context.next();

	}
	@Override
	public LoggerHandler customFormatter(Function<HttpServerRequest, String> formatter) {
		if (format != LoggerFormat.CUSTOM) {
			throw new IllegalStateException("Setting a formatter requires the handler to be set to CUSTOM format");
		}

		this.customFormatter = formatter;

		return this;
	}
}

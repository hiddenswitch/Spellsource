/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.client.impl;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Timer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.StreamResetException;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.client.GrpcClientResponse;
import io.vertx.grpc.common.GrpcErrorException;
import io.vertx.grpc.common.*;
import io.vertx.grpc.common.impl.GrpcMessageImpl;
import io.vertx.grpc.common.impl.GrpcWriteStreamBase;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientRequestImpl<Req, Resp> extends GrpcWriteStreamBase<GrpcClientRequestImpl<Req, Resp>, Req> implements GrpcClientRequest<Req, Resp> {

  private final HttpClientRequest httpRequest;
  private final boolean scheduleDeadline;
  private ServiceName serviceName;
  private String methodName;
  private Future<GrpcClientResponse<Req, Resp>> response;
  private long timeout;
  private TimeUnit timeoutUnit;
  private String timeoutHeader;
  private Timer deadline;
  private boolean cancelled;

  public GrpcClientRequestImpl(HttpClientRequest httpRequest,
                               long maxMessageSize,
                               boolean scheduleDeadline,
                               GrpcMessageEncoder<Req> messageEncoder,
                               GrpcMessageDecoder<Resp> messageDecoder) {
    super( ((PromiseInternal<?>)httpRequest.response()).context(), "application/grpc", httpRequest, messageEncoder);
    this.httpRequest = httpRequest;
    this.scheduleDeadline = scheduleDeadline;
    this.timeout = 0L;
    this.timeoutUnit = null;
    this.timeoutHeader = null;
    this.response = httpRequest
      .response()
      .compose(httpResponse -> {
        String msg = null;
        String statusHeader = httpResponse.getHeader(GrpcHeaderNames.GRPC_STATUS);
        GrpcStatus status = statusHeader != null ? GrpcStatus.valueOf(Integer.parseInt(statusHeader)) : null;
        WireFormat format = null;
        if (status == null) {
          String contentType = httpResponse.getHeader(HttpHeaders.CONTENT_TYPE);
          if (contentType != null) {
            format = GrpcMediaType.parseContentType(contentType, "application/grpc");
          }
          if (contentType == null) {
            msg = "HTTP response missing content-type header";
          } else {
            msg = "Invalid HTTP response content-type header";
          }
        }
        if (format != null || status != null) {
          GrpcClientResponseImpl<Req, Resp> grpcResponse = new GrpcClientResponseImpl<>(
            context,
            this,
            format,
            status,
            httpResponse,
            messageDecoder);
          grpcResponse.init(this, maxMessageSize);
          grpcResponse.invalidMessageHandler(invalidMsg -> {
            cancel();
            grpcResponse.tryFail(invalidMsg);
          });
          return Future.succeededFuture(grpcResponse);
        }
        httpResponse.request().reset(GrpcError.CANCELLED.http2ResetCode);
        return context().failedFuture(msg);
      }, err -> {
        if (err instanceof StreamResetException) {
          err = GrpcErrorException.create((StreamResetException) err);
        }
        return Future.failedFuture(err);
      });
  }

  @Override
  protected Future<Void> sendHead() {
    return httpRequest.sendHead();
  }

  @Override
  public GrpcClientRequest<Req, Resp> serviceName(ServiceName serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  @Override
  public GrpcClientRequest<Req, Resp> fullMethodName(String fullMethodName) {
    if (isHeadersSent()) {
      throw new IllegalStateException("Request already sent");
    }
    int idx = fullMethodName.lastIndexOf('/');
    if (idx == -1) {
      throw new IllegalArgumentException();
    }
    this.serviceName = ServiceName.create(fullMethodName.substring(0, idx));
    this.methodName = fullMethodName.substring(idx + 1);
    return this;
  }

  @Override
  public GrpcClientRequest<Req, Resp> methodName(String methodName) {
    this.methodName = methodName;
    return this;
  }

  @Override
  public GrpcClientRequest<Req, Resp> timeout(long timeout, TimeUnit unit) {
    if (timeout < 0L) {
      throw new IllegalArgumentException("Timeout must be positive");
    }
    if (isHeadersSent()) {
      throw new IllegalStateException("Timeout must be set before sending request headers");
    }
    String headerValue = toTimeoutHeader(timeout, unit);
    if (headerValue == null) {
      throw new IllegalArgumentException("Not a valid gRPC timeout value (" + timeout + ',' + unit + ')');
    }
    this.timeout = timeout;
    this.timeoutUnit = unit;
    this.timeoutHeader = headerValue;
    return this;
  }

  @Override
  public Timer deadline() {
    return deadline;
  }

  @Override
  public GrpcClientRequest<Req, Resp> idleTimeout(long timeout) {
    httpRequest.idleTimeout(timeout);
    return this;
  }

  @Override
  protected void setHeaders(String contentType, MultiMap headers) {
    ServiceName serviceName = this.serviceName;
    String methodName = this.methodName;
    if (serviceName == null) {
      throw new IllegalStateException();
    }
    if (methodName == null) {
      throw new IllegalStateException();
    }
    if (headers != null) {
      MultiMap requestHeaders = httpRequest.headers();
      for (Map.Entry<String, String> header : headers) {
        requestHeaders.add(header.getKey(), header.getValue());
      }
    }
    if (timeout > 0L) {
      httpRequest.putHeader(GrpcHeaderNames.GRPC_TIMEOUT, timeoutHeader);
    }
    String uri = serviceName.pathOf(methodName);
    httpRequest.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
    if (encoding != null) {
      httpRequest.putHeader(GrpcHeaderNames.GRPC_ENCODING, encoding);
    }
    httpRequest.putHeader(GrpcHeaderNames.GRPC_ACCEPT_ENCODING, "gzip");
    httpRequest.putHeader(HttpHeaderNames.TE, "trailers");
    httpRequest.setChunked(true);
    httpRequest.setURI(uri);
    if (scheduleDeadline && timeout > 0L) {
      Timer timer = context.timer(timeout, timeoutUnit);
      deadline = timer;
      timer.onSuccess(v -> {
        cancel();
      });
    }
  }

  @Override
  protected void setTrailers(MultiMap trailers) {
  }

  @Override
  protected Future<Void> sendMessage(Buffer message, boolean compressed) {
    return httpRequest.write(GrpcMessageImpl.encode(message, compressed, false));
  }

  @Override
  protected Future<Void> sendEnd() {
    return httpRequest.end();
  }

  void cancelTimeout() {
    Timer timer = deadline;
    if (timer != null && timer.cancel()) {
      deadline = null;
    }
  }

  @Override public Future<GrpcClientResponse<Req, Resp>> response() {
    return response;
  }

  @Override
  public void cancel() {
    if (cancelled) {
      return;
    }
    cancelled = true;
    context.execute(() -> {
      boolean responseEnded;
      if (response.failed()) {
        return;
      } else if (response.succeeded()) {
        GrpcClientResponse<Req, Resp> resp = response.result();
        if (resp.end().failed()) {
          return;
        } else {
          responseEnded = resp.end().succeeded();
        }
      } else {
        responseEnded = false;
      }
      if (!isTrailersSent() || !responseEnded) {
        httpRequest
          .reset(GrpcError.CANCELLED.http2ResetCode)
          .onSuccess(v -> handleError(GrpcError.CANCELLED));
      }
    });
  }

  @Override
  public HttpConnection connection() {
    return httpRequest.connection();
  }

  private static final EnumMap<TimeUnit, Character> GRPC_TIMEOUT_UNIT_SUFFIXES = new EnumMap<>(TimeUnit.class);

  static {
    GRPC_TIMEOUT_UNIT_SUFFIXES.put(TimeUnit.NANOSECONDS, 'n');
    GRPC_TIMEOUT_UNIT_SUFFIXES.put(TimeUnit.MICROSECONDS, 'u');
    GRPC_TIMEOUT_UNIT_SUFFIXES.put(TimeUnit.MILLISECONDS, 'm');
    GRPC_TIMEOUT_UNIT_SUFFIXES.put(TimeUnit.SECONDS, 'S');
    GRPC_TIMEOUT_UNIT_SUFFIXES.put(TimeUnit.MINUTES, 'M');
    GRPC_TIMEOUT_UNIT_SUFFIXES.put(TimeUnit.HOURS, 'H');
  }

  private static final TimeUnit[] GRPC_TIMEOUT_UNITS = {
    TimeUnit.NANOSECONDS,
    TimeUnit.MICROSECONDS,
    TimeUnit.MILLISECONDS,
    TimeUnit.SECONDS,
    TimeUnit.MINUTES,
    TimeUnit.HOURS,
  };

  /**
   * Compute timeout header, returns {@code null} when the timeout value is not valid.
   *
   * @param timeout the timeout
   * @param timeoutUnit the timeout unit
   * @return the grpc-timeout header value, e.g. 1M (1 minute)
   */
  public static String toTimeoutHeader(long timeout, TimeUnit timeoutUnit) {
    for (TimeUnit grpcTimeoutUnit : GRPC_TIMEOUT_UNITS) {
      String res = toTimeoutHeader(timeout, timeoutUnit, grpcTimeoutUnit);
      if (res != null) {
        return res;
      }
    }
    return null;
  }

  private static String toTimeoutHeader(long timeout, TimeUnit srcUnit, TimeUnit grpcTimeoutUnit) {
    long v = grpcTimeoutUnit.convert(timeout, srcUnit);
    if (v < 1_000_000_00) {
      return Long.toString(v) + GRPC_TIMEOUT_UNIT_SUFFIXES.get(grpcTimeoutUnit);
    }
    return null;
  }
}

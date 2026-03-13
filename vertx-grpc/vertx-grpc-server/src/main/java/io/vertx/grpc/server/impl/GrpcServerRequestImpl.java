/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.server.impl;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Timer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.internal.ContextInternal;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.common.*;
import io.vertx.grpc.common.impl.GrpcMessageDeframer;
import io.vertx.grpc.common.impl.GrpcMethodCall;
import io.vertx.grpc.common.impl.GrpcReadStreamBase;
import io.vertx.grpc.common.impl.GrpcWriteStreamBase;
import io.vertx.grpc.server.GrpcProtocol;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.StatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class GrpcServerRequestImpl<Req, Resp> extends GrpcReadStreamBase<GrpcServerRequestImpl<Req, Resp>, Req> implements GrpcServerRequest<Req, Resp> {

  private static final Pattern TIMEOUT_PATTERN = Pattern.compile("([0-9]{1,8})([HMSmun])");

  private static final Map<String, TimeUnit> TIMEOUT_MAPPING;

  static {
    Map<String, TimeUnit> timeoutMapping = new HashMap<>();
    timeoutMapping.put("H", TimeUnit.HOURS);
    timeoutMapping.put("M", TimeUnit.MINUTES);
    timeoutMapping.put("S", TimeUnit.SECONDS);
    timeoutMapping.put("m", TimeUnit.MILLISECONDS);
    timeoutMapping.put("u", TimeUnit.MICROSECONDS);
    timeoutMapping.put("n", TimeUnit.NANOSECONDS);
    TIMEOUT_MAPPING = timeoutMapping;
  }

  private static long parseTimeout(String timeout) {
    Matcher matcher = TIMEOUT_PATTERN.matcher(timeout);
    if (matcher.matches()) {
      long value = Long.parseLong(matcher.group(1));
      TimeUnit unit = TIMEOUT_MAPPING.get(matcher.group(2));
      return unit.toMillis(value);
    } else {
      return 0L;
    }
  }

  final HttpServerRequest httpRequest;
  final long timeout;
  final GrpcProtocol protocol;
  private RoutingContext routingContext;
  private GrpcServerResponseImpl<Req, Resp> response;
  private final GrpcMethodCall methodCall;
  private Timer deadline;

  public GrpcServerRequestImpl(ContextInternal context,
                               GrpcProtocol protocol,
                               WireFormat format,
                               HttpServerRequest httpRequest,
                               GrpcMessageDeframer messageDeframer,
                               GrpcMessageDecoder<Req> messageDecoder,
                               GrpcMethodCall methodCall) {
    super(context, httpRequest, httpRequest.headers().get(GrpcHeaderNames.GRPC_ENCODING), format, messageDeframer, messageDecoder);
    String timeoutHeader = httpRequest.getHeader(GrpcHeaderNames.GRPC_TIMEOUT);
    long timeout = timeoutHeader != null ? parseTimeout(timeoutHeader) : 0L;

    this.protocol = protocol;
    this.timeout = timeout;
    this.httpRequest = httpRequest;
    this.methodCall = methodCall;
  }

  ContextInternal context() {
    return context;
  }

  public void init(GrpcWriteStreamBase ws, boolean scheduleDeadline, long maxMessageSize) {
    this.response = (GrpcServerResponseImpl<Req, Resp>) ws;
    super.init(ws, maxMessageSize);
    if (timeout > 0L) {
      if (scheduleDeadline) {
        Timer timer = context.timer(timeout, TimeUnit.MILLISECONDS);
        deadline = timer;
        timer.onSuccess(v -> {
          response.handleTimeout();
        });
      }
    }
  }

  void cancelTimeout() {
    Timer timer = deadline;
    if (timer != null) {
      deadline = null;
      timer.cancel();
    }
  }

  public String fullMethodName() {
    return methodCall.fullMethodName();
  }

  @Override
  public MultiMap headers() {
    return httpRequest.headers();
  }

  @Override
  public ServiceName serviceName() {
    return methodCall.serviceName();
  }

  @Override
  public String methodName() {
    return methodCall.methodName();
  }

  @Override
  public GrpcServerRequestImpl<Req, Resp> handler(Handler<Req> handler) {
    if (handler != null) {
      return messageHandler(msg -> {
        Req decoded;
        try {
          decoded = decodeMessage(msg);
        } catch (CodecException e) {
          response.cancel();
          return;
        }
        try {
          handler.handle(decoded);
        } catch (Exception e) {
          response.fail(e);
        }
      });
    } else {
      return messageHandler(null);
    }
  }

  public GrpcServerResponseImpl<Req, Resp> response() {
    return response;
  }

  @Override
  public HttpConnection connection() {
    return httpRequest.connection();
  }

  @Override
  public long timeout() {
    return timeout;
  }

  @Override
  public Timer deadline() {
    return deadline;
  }

  public void setRoutingContext(RoutingContext routingContext) {
    this.routingContext = routingContext;
  }

  @Override
  public RoutingContext routingContext() {
    return routingContext;
  }
}

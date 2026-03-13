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

import io.grpc.MethodDescriptor;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.internal.http.HttpServerRequestInternal;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.spi.context.storage.AccessMode;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.common.*;
import io.vertx.grpc.common.impl.GrpcMethodCall;
import io.vertx.grpc.server.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcServerImpl implements GrpcServer {

  private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("application/grpc(-web(-text)?)?(\\+(json|proto))?");

  private static final Logger log = LoggerFactory.getLogger(GrpcServer.class);

  private final GrpcServerOptions options;
  private Handler<GrpcServerRequest<Buffer, Buffer>> requestHandler;

  private final List<Service> services = new ArrayList<>();
  private final Map<String, List<MethodCallHandler<?, ?>>> methodCallHandlers = new HashMap<>();

  private final List<GrpcHttpInvoker> invokers;

  public GrpcServerImpl(Vertx vertx, GrpcServerOptions options) {

    ServiceLoader<GrpcHttpInvoker> loader = ServiceLoader.load(GrpcHttpInvoker.class);
    this.invokers = loader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
    this.options = new GrpcServerOptions(Objects.requireNonNull(options, "options is null"));
  }

  // Internal pojo, the name does not matter much at the moment
  private static class Details {
    final GrpcProtocol protocol;
    final WireFormat format;
    Details(GrpcProtocol protocol, WireFormat format) {
      this.protocol = protocol;
      this.format = format;
    }
  }

  private Details determine(String contentType) {
    WireFormat format;
    GrpcProtocol protocol;
    if (contentType != null) {
      Matcher matcher = CONTENT_TYPE_PATTERN.matcher(contentType);
      if (matcher.matches()) {
        if (matcher.group(1) != null) {
          protocol = matcher.group(2) == null ? GrpcProtocol.WEB : GrpcProtocol.WEB_TEXT;
        } else {
          protocol = GrpcProtocol.HTTP_2;
        }
        if (matcher.group(3) != null) {
          switch (matcher.group(4)) {
            case "proto":
              format = WireFormat.PROTOBUF;
              break;
            case "json":
              format = WireFormat.JSON;
              break;
            default:
              throw new UnsupportedOperationException("Not possible");
          }
        } else {
          format = WireFormat.PROTOBUF;
        }
        return new Details(protocol, format);
      } else {
        if (GrpcProtocol.TRANSCODING.mediaType().equals(contentType)) {
          protocol = GrpcProtocol.TRANSCODING;
          format = WireFormat.JSON;
          return new Details(protocol, format);
        } else {
          return null;
        }
      }
    } else {
      return null;
    }
  }

  @Override
  public void handle(HttpServerRequest httpRequest) {
    String contentType = httpRequest.getHeader(CONTENT_TYPE);
    Details details;
    if (contentType != null && (details = determine(contentType)) != null) {
      int errorCode = validate(httpRequest.version(), details.protocol, details.format);
      if (errorCode > 0) {
        httpRequest.response().setStatusCode(errorCode).end();
        return;
      }
    } else {
      httpRequest.response().setStatusCode(415).end();
      return;
    }

    GrpcMethodCall methodCall = new GrpcMethodCall(httpRequest.path());
    String path = httpRequest.path();
    while (true) {
      List<MethodCallHandler<?, ?>> mchList = methodCallHandlers.get(path);
      if (mchList != null) {
        for (MethodCallHandler<?, ?> mch : mchList) {
          if (handle(mch, httpRequest, methodCall, details.protocol, details.format)) {
            return;
          }
        }
      }
      int idx = path.lastIndexOf('/');
      if (idx <= 0) {
        break;
      }
      path = path.substring(0, idx);
    }

    // Generic handling
    Handler<GrpcServerRequest<Buffer, Buffer>> handler = requestHandler;
    if (handler != null) {
      handle(new MethodCallHandler<>(null, GrpcMessageDecoder.IDENTITY, GrpcMessageEncoder.IDENTITY, handler), httpRequest, methodCall, details.protocol, details.format);
    } else {
      String msg = "Method not found: " + httpRequest.path().substring(1);
      HttpServerResponse response = httpRequest.response();
      boolean webText = true;
      switch (details.protocol) {
        case HTTP_2:
        case WEB:
        case WEB_TEXT:
          response.setStatusCode(200);
          response.putHeader(HttpHeaders.CONTENT_TYPE, details.protocol.mediaType());
          response.putHeader(GrpcHeaderNames.GRPC_STATUS, GrpcStatus.UNIMPLEMENTED.toString());
          response.putHeader(GrpcHeaderNames.GRPC_MESSAGE, msg);
          response.end();
          break;
        default:
          response
            .setStatusCode(500)
            .end();
          break;
      }
    }
  }

  private int validate(HttpVersion version, GrpcProtocol protocol, WireFormat format) {
    // Check HTTP version compatibility
    if (!protocol.accepts(version)) {
      log.trace(protocol.name() + " not supported on " + version + ", sending error 415");
      return 415;
    }
    // Check config
    if (!options.isProtocolEnabled(protocol)) {
      log.trace(protocol + " is not supported, sending error 415");
      return 415;
    }
    return -1;
  }

  private <Req, Resp> void handle(GrpcInvocation<Req, Resp> invocation, Handler<GrpcServerRequest<Req, Resp>> handler) {
    handle(invocation.grpcRequest, invocation.grpcResponse, handler);
  }

  private <Req, Resp> boolean handle(MethodCallHandler<Req, Resp> method, HttpServerRequest httpRequest, GrpcMethodCall methodCall, GrpcProtocol protocol, WireFormat format) {
    io.vertx.core.internal.ContextInternal context = ((HttpServerRequestInternal) httpRequest).context();

    GrpcServerRequestImpl<Req, Resp> grpcRequest;
    GrpcServerResponseImpl<Req, Resp> grpcResponse;
    switch (protocol) {
      case HTTP_2:
        if (method.method != null && !httpRequest.path().equals("/" + method.method.fullMethodName())) {
          return false;
        }
        grpcRequest = new Http2GrpcServerRequest<>(
          context,
          protocol,
          format,
          httpRequest,
          method.messageDecoder,
          methodCall);
        grpcResponse = new Http2GrpcServerResponse<>(
          context,
          grpcRequest,
          protocol,
          httpRequest.response(),
          method.messageEncoder);
        break;
      case WEB:
      case WEB_TEXT:
        if (method.method != null && !httpRequest.path().equals("/" + method.method.fullMethodName())) {
          return false;
        }
        grpcRequest = new WebGrpcServerRequest<>(
          context,
          protocol,
          format,
          options.getMaxMessageSize(),
          httpRequest,
          method.messageDecoder,
          methodCall);
        grpcResponse = new WebGrpcServerResponse<>(
          context,
          grpcRequest,
          protocol,
          httpRequest.response(),
          method.messageEncoder);
        break;
      case TRANSCODING:
        grpcRequest = null;
        grpcResponse = null;
        for (GrpcHttpInvoker invoker : invokers) {
          GrpcInvocation<Req, Resp> invocation = invoker.accept(httpRequest, method.method);
          if (invocation != null) {
            grpcRequest = invocation.grpcRequest;
            grpcResponse = invocation.grpcResponse;
            break;
          }
        }
        break;
      default:
        throw new AssertionError();
    }
    if (grpcRequest == null || grpcResponse == null) {
      return false;
    }
    grpcResponse.format(format);
    handle(grpcRequest, grpcResponse, method);
    return true;
  }

  private <Req, Resp> void handle(GrpcServerRequestImpl<Req, Resp> grpcRequest,
                                  GrpcServerResponseImpl<Req, Resp> grpcResponse,
                                  Handler<GrpcServerRequest<Req, Resp>> handler) {
    if (options.getDeadlinePropagation() && grpcRequest.timeout() > 0L) {
      long deadline = System.currentTimeMillis() + grpcRequest.timeout;
      grpcRequest.context().putLocal(GrpcLocal.CONTEXT_LOCAL_KEY, AccessMode.CONCURRENT, new GrpcLocal(deadline));
    }
    // Inject RoutingContext if available
    RoutingContext rc = CURRENT_ROUTING_CONTEXT.get();
    if (rc != null) {
      grpcRequest.setRoutingContext(rc);
    }
    grpcResponse.init();
    grpcRequest.init(grpcResponse, options.getScheduleDeadlineAutomatically(), options.getMaxMessageSize());
    grpcRequest.invalidMessageHandler(invalidMsg -> {
      if (invalidMsg instanceof MessageSizeOverflowException) {
        grpcRequest.response().status(GrpcStatus.RESOURCE_EXHAUSTED).end();
      } else {
        grpcResponse.cancel();
      }
    });
    grpcRequest.context().dispatch(grpcRequest, handler);
  }

  public GrpcServer callHandler(Handler<GrpcServerRequest<Buffer, Buffer>> handler) {
    this.requestHandler = handler;
    return this;
  }

  private <Req, Resp> void registerMethodCallHandler(String path, MethodCallHandler<Req, Resp> mch) {
    methodCallHandlers.computeIfAbsent(path, k -> new ArrayList<>()).add(mch);
  }

  private <Req, Resp> void unregisterMethodCallHandler(String path, ServiceMethod<Req, Resp> serviceMethod) {
    methodCallHandlers.computeIfPresent(path, (p, registrations) -> {
      Iterator<MethodCallHandler<?, ?>> it = registrations.iterator();
      while (it.hasNext()) {
        MethodCallHandler<?, ?> mch = it.next();
        if (mch.method.equals(serviceMethod)) {
          it.remove();
        }
      }
      return registrations.isEmpty() ? null : registrations;
    });
  }

  @Override
  public <Req, Resp> GrpcServer callHandler(ServiceMethod<Req, Resp> serviceMethod, Handler<GrpcServerRequest<Req, Resp>> handler) {
    if (handler != null) {
      MethodCallHandler<Req, Resp> p = new MethodCallHandler<>(serviceMethod, serviceMethod.decoder(), serviceMethod.encoder(), handler);
      if (serviceMethod instanceof MountPoint) {
        MountPoint<Req, Resp> mountPoint = (MountPoint<Req, Resp>) serviceMethod;
        List<String> paths = mountPoint.paths();
        for (String path : paths) {
          registerMethodCallHandler(path, p);
        }
      }
      registerMethodCallHandler("/" + serviceMethod.fullMethodName(), p);
    } else {
      if (serviceMethod instanceof MountPoint) {
        MountPoint<Req, Resp> mountPoint = (MountPoint<Req, Resp>) serviceMethod;
        List<String> paths = mountPoint.paths();
        for (String path : paths) {
          unregisterMethodCallHandler(path, serviceMethod);
        }
      }
      unregisterMethodCallHandler("/" + serviceMethod.fullMethodName(), serviceMethod);
    }
    return this;
  }

  @Override
  public GrpcServer addService(Service service) {
    for (Service s : this.services) {
      if (s.name().equals(service.name())) {
        throw new IllegalStateException("Duplicated name: " + service.name().name());
      }
    }

    this.services.add(service);
    service.bind(this);

    return this;
  }

  @Override
  public List<Service> services() {
    return Collections.unmodifiableList(services);
  }

  // Thread-local to pass RoutingContext through handle() to the request
  private static final ThreadLocal<RoutingContext> CURRENT_ROUTING_CONTEXT = new ThreadLocal<>();

  public void handle(HttpServerRequest httpRequest, RoutingContext routingContext) {
    CURRENT_ROUTING_CONTEXT.set(routingContext);
    try {
      handle(httpRequest);
    } finally {
      CURRENT_ROUTING_CONTEXT.remove();
    }
  }

  @Override
  public Handler<RoutingContext> routeHandler() {
    return ctx -> handle(ctx.request(), ctx);
  }

  @Override
  public <Req, Resp> GrpcServer callHandler(MethodDescriptor<Req, Resp> methodDesc, Handler<GrpcServerRequest<Req, Resp>> handler) {
    if (handler != null) {
      GrpcMessageDecoder<Req> decoder = GrpcMessageDecoder.unmarshaller(methodDesc.getRequestMarshaller());
      GrpcMessageEncoder<Resp> encoder = GrpcMessageEncoder.marshaller(methodDesc.getResponseMarshaller());
      ServiceMethod<Req, Resp> serviceMethod = ServiceMethod.server(
        ServiceName.create(methodDesc.getServiceName()),
        methodDesc.getBareMethodName(),
        encoder,
        decoder);
      return callHandler(serviceMethod, handler);
    } else {
      ServiceMethod<Req, Resp> serviceMethod = ServiceMethod.server(
        ServiceName.create(methodDesc.getServiceName()),
        methodDesc.getBareMethodName(),
        null,
        null);
      // Unregister
      unregisterMethodCallHandler("/" + serviceMethod.fullMethodName(), serviceMethod);
      return this;
    }
  }

  private static class MethodCallHandler<Req, Resp> implements Handler<GrpcServerRequest<Req, Resp>> {

    final ServiceMethod<Req, Resp> method;
    final GrpcMessageDecoder<Req> messageDecoder;
    final GrpcMessageEncoder<Resp> messageEncoder;
    final Handler<GrpcServerRequest<Req, Resp>> handler;

    MethodCallHandler(ServiceMethod<Req, Resp> method, GrpcMessageDecoder<Req> messageDecoder, GrpcMessageEncoder<Resp> messageEncoder, Handler<GrpcServerRequest<Req, Resp>> handler) {
      this.method = method;
      this.messageDecoder = messageDecoder;
      this.messageEncoder = messageEncoder;
      this.handler = handler;
    }

    @Override
    public void handle(GrpcServerRequest<Req, Resp> grpcRequest) {
      try {
        handler.handle(grpcRequest);
      } catch (Exception e) {
        grpcRequest.response().fail(e);
      }
    }
  }
}

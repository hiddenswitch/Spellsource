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

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.internal.ContextInternal;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcHeaderNames;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.impl.GrpcMessageImpl;
import io.vertx.grpc.common.impl.GrpcWriteStreamBase;
import io.vertx.grpc.common.impl.Utils;
import io.vertx.grpc.server.GrpcProtocol;
import io.vertx.grpc.server.GrpcServerResponse;
import io.vertx.grpc.server.StatusException;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class GrpcServerResponseImpl<Req, Resp> extends GrpcWriteStreamBase<GrpcServerResponseImpl<Req, Resp>, Resp> implements GrpcServerResponse<Req, Resp> {

  private static final Pattern COMMA_SEPARATOR = Pattern.compile(" *, *");
  private static final Set<String> GZIP_ACCEPT_ENCODING = Collections.singleton("gzip");

  private final GrpcServerRequestImpl<Req, Resp> request;
  private final HttpServerResponse httpResponse;
  private GrpcStatus status = GrpcStatus.OK;
  private String statusMessage;
  private boolean trailersOnly;
  private boolean cancelled;
  private Set<String> acceptedEncodings;

  public GrpcServerResponseImpl(ContextInternal context,
                                GrpcServerRequestImpl<Req, Resp> request,
                                GrpcProtocol protocol,
                                HttpServerResponse httpResponse,
                                GrpcMessageEncoder<Resp> encoder) {
    super(context, protocol.mediaType(), httpResponse, encoder);
    this.request = request;
    this.httpResponse = httpResponse;
  }

  public GrpcServerResponse<Req, Resp> status(GrpcStatus status) {
    Objects.requireNonNull(status);
    this.status = status;
    return this;
  }

  @Override
  public GrpcServerResponse<Req, Resp> statusMessage(String msg) {
    this.statusMessage = msg;
    return this;
  }

  public void handleTimeout() {
    if (!isCancelled()) {
      if (!isTrailersSent()) {
        status(GrpcStatus.DEADLINE_EXCEEDED);
        end();
      } else {
        cancel();
      }
    }
  }

  @Override
  public void cancel() {
    if (cancelled) {
      return;
    }
    cancelled = true;
    Future<Void> fut = request.end();
    boolean requestEnded;
    if (fut.failed()) {
      return;
    } else {
      requestEnded = fut.succeeded();
    }
    if (!requestEnded || !isTrailersSent()) {
      sendCancel();
    }
  }

  public void fail(Throwable failure) {
    if (failure instanceof StatusException) {
      StatusException se = (StatusException) failure;
      this.status = se.status();
      this.statusMessage = se.message();
    } else {
      this.status = mapStatus(failure);
    }
    end();
  }

  // TODO : remove this
  public boolean isTrailersOnly() {
    return trailersOnly;
  }

  public GrpcStatus status() {
    return status;
  }

  protected void sendCancel() {
    httpResponse
      .reset(GrpcError.CANCELLED.http2ResetCode)
      .onSuccess(v -> handleError(GrpcError.CANCELLED));
  }

  protected void setHeaders(String contentType, MultiMap grpcHeaders) {
    MultiMap httpHeaders = httpResponse.headers();
    httpHeaders.set("content-type", contentType);
    encodeGrpcHeaders(grpcHeaders, httpHeaders);
    if (trailersOnly) {
      encodeGrpcStatus(httpHeaders);
    }
  }

  protected void encodeGrpcHeaders(MultiMap grpcHeaders, MultiMap httpHeaders) {
    if (grpcHeaders != null && !grpcHeaders.isEmpty()) {
      for (Map.Entry<String, String> header : grpcHeaders) {
        httpHeaders.add(header.getKey(), header.getValue());
      }
    }
  }

  protected void setTrailers(MultiMap grpcTrailers) {
    MultiMap httpTrailers;
    if (trailersOnly) {
      httpTrailers = httpResponse.headers();
    } else {
      httpTrailers = httpResponse.trailers();
    }
    encodeGrpcTrailers(grpcTrailers, httpTrailers);
    encodeGrpcStatus(httpTrailers);
  }

  protected final void encodeGrpcTrailers(MultiMap grpcTrailers, MultiMap httpTrailers) {
    if (grpcTrailers != null && !grpcTrailers.isEmpty()) {
      for (Map.Entry<String, String> header : grpcTrailers) {
        httpTrailers.add(header.getKey(), header.getValue());
      }
    }
  }

  /**
   * Encode grpc status and status message in the specified {@code entries} map.
   *
   * @param entries the map updated with grpc specific headers
   */
  protected void encodeGrpcStatus(MultiMap entries) {
    if (!entries.contains(GrpcHeaderNames.GRPC_STATUS)) {
      entries.set(GrpcHeaderNames.GRPC_STATUS, status.toString());
    }
    if (status != GrpcStatus.OK) {
      String msg = statusMessage;
      if (msg != null && !entries.contains(GrpcHeaderNames.GRPC_MESSAGE)) {
        entries.set(GrpcHeaderNames.GRPC_MESSAGE, Utils.utf8PercentEncode(msg));
      }
    } else {
      entries.remove(GrpcHeaderNames.GRPC_MESSAGE);
    }
  }

  @Override
  public Set<String> acceptedEncodings() {
    if (acceptedEncodings == null) {
      String acceptEncodingHeader = request.headers().get("grpc-accept-encoding");
      if (acceptEncodingHeader != null) {
        if (acceptEncodingHeader.equals("gzip")) {
          acceptedEncodings = GZIP_ACCEPT_ENCODING;
        } else {
          acceptedEncodings = new HashSet<>(2);
          String[] encodings = COMMA_SEPARATOR.split(acceptEncodingHeader);
          for (String encoding : encodings) {
            acceptedEncodings.add(encoding.trim());
          }
        }
      } else {
        acceptedEncodings = Collections.emptySet();
      }
    }
    return acceptedEncodings;
  }

  @Override
  protected Future<Void> sendMessage(Buffer message, boolean compressed) {
    return httpResponse.write(encodeMessage(message, compressed, false));
  }

  protected Future<Void> sendEnd() {
    request.cancelTimeout();
    return httpResponse.end();
  }

  @Override
  protected Future<Void> sendHead() {
    return httpResponse.writeHead();
  }

  protected Buffer encodeMessage(Buffer message, boolean compressed, boolean trailer) {
    return GrpcMessageImpl.encode(message, compressed, trailer);
  }

  private static GrpcStatus mapStatus(Throwable t) {
    if (t instanceof StatusException) {
      return ((StatusException)t).status();
    } else if (t instanceof UnsupportedOperationException) {
      return GrpcStatus.UNIMPLEMENTED;
    } else {
      return GrpcStatus.UNKNOWN;
    }
  }

  private boolean headersSent;

  @Override
  protected Future<Void> writeMessage(GrpcMessage message, boolean end) {
    if (!headersSent) {
      headersSent = true;
      trailersOnly = status != GrpcStatus.OK && end;
    }
    return super.writeMessage(message, end);
  }
}

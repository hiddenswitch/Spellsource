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
package io.vertx.grpc.server.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.grpc.common.CodecException;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.impl.GrpcMessageImpl;
import io.vertx.grpc.common.impl.Utils;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.GrpcServerResponse;

import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcServerResponseImpl<Req, Resp> implements GrpcServerResponse<Req, Resp> {

  private final GrpcServerRequestImpl<Req, Resp> request;
  private final HttpServerResponse httpResponse;
  private final GrpcMessageEncoder<Resp> encoder;
  private String encoding;
  private GrpcStatus status = GrpcStatus.OK;
  private String statusMessage;
  private boolean headersSent;
  private boolean trailersSent;
  private boolean cancelled;
  private MultiMap headers, trailers;

  public GrpcServerResponseImpl(GrpcServerRequestImpl<Req, Resp> request, HttpServerResponse httpResponse, GrpcMessageEncoder<Resp> encoder) {
    this.request = request;
    this.httpResponse = httpResponse;
    this.encoder = encoder;
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

  public GrpcServerResponse<Req, Resp> encoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  @Override
  public MultiMap headers() {
    if (headersSent) {
      throw new IllegalStateException("Headers already sent");
    }
    if (headers == null) {
      headers = MultiMap.caseInsensitiveMultiMap();
    }
    return headers;
  }

  @Override
  public MultiMap trailers() {
    if (trailersSent) {
      throw new IllegalStateException("Trailers already sent");
    }
    if (trailers == null) {
      trailers = MultiMap.caseInsensitiveMultiMap();
    }
    return trailers;
  }

  @Override
  public GrpcServerResponseImpl<Req, Resp> exceptionHandler(Handler<Throwable> handler) {
    httpResponse.exceptionHandler(handler);
    return this;
  }

  @Override
  public Future<Void> write(Resp message) {
    return writeMessage(encoder.encode(message));
  }

  @Override
  public void write(Resp resp, Handler<AsyncResult<Void>> handler) {
    write(resp).onComplete(handler);
  }

  @Override
  public Future<Void> end(Resp message) {
    return endMessage(encoder.encode(message));
  }

  @Override
  public Future<Void> writeMessage(GrpcMessage data) {
    return writeMessage(data, false);
  }

  @Override
  public Future<Void> endMessage(GrpcMessage message) {
    return writeMessage(message, true);
  }

  public Future<Void> end() {
    return writeMessage(null, true);
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    end().onComplete(handler);
  }

  @Override
  public GrpcServerResponse<Req, Resp> setWriteQueueMaxSize(int maxSize) {
    httpResponse.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return httpResponse.writeQueueFull();
  }

  @Override
  public GrpcServerResponse<Req, Resp> drainHandler(Handler<Void> handler) {
    httpResponse.drainHandler(handler);
    return this;
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
    if (!requestEnded || !trailersSent) {
      httpResponse.reset(GrpcError.CANCELLED.http2ResetCode);
    }
  }

  private Future<Void> writeMessage(GrpcMessage message, boolean end) {

    if (cancelled) {
      throw new IllegalStateException("The stream has been cancelled");
    }
    if (trailersSent) {
      throw new IllegalStateException("The stream has been closed");
    }

    if (message == null && !end) {
      throw new IllegalStateException();
    }

    if (encoding != null && message != null && !encoding.equals(message.encoding())) {
      switch (encoding) {
        case "gzip":
          message = GrpcMessageEncoder.GZIP.encode(message.payload());
          break;
        case "identity":
          if (!message.encoding().equals("identity")) {
            if (!message.encoding().equals("gzip")) {
              return Future.failedFuture("Encoding " + message.encoding() + " is not supported");
            }
            Buffer decoded;
            try {
              decoded = GrpcMessageDecoder.GZIP.decode(message);
            } catch (CodecException e) {
              return Future.failedFuture(e);
            }
            message = GrpcMessage.message("identity", decoded);
          }
          break;
      }
    }

    boolean trailersOnly = status != GrpcStatus.OK && !headersSent && end;

    MultiMap responseHeaders = httpResponse.headers();
    if (!headersSent) {
      headersSent = true;
      if (headers != null && headers.size() > 0) {
        for (Map.Entry<String, String> header : headers) {
          responseHeaders.add(header.getKey(), header.getValue());
        }
      }
      responseHeaders.set("content-type", "application/grpc");
      responseHeaders.set("grpc-encoding", encoding);
      responseHeaders.set("grpc-accept-encoding", "gzip");
    }

    if (end) {
      if (!trailersSent) {
        trailersSent = true;
      }
      MultiMap responseTrailers;
      if (trailersOnly) {
        responseTrailers = httpResponse.headers();
      } else {
        responseTrailers = httpResponse.trailers();
      }

      if (trailers != null && trailers.size() > 0) {
        for (Map.Entry<String, String> trailer : trailers) {
          responseTrailers.add(trailer.getKey(), trailer.getValue());
        }
      }
      if (!responseHeaders.contains("grpc-status")) {
        responseTrailers.set("grpc-status", status.toString());
      }
      if (status != GrpcStatus.OK) {
        String msg = statusMessage;
        if (msg != null && !responseHeaders.contains("grpc-status-message")) {
          responseTrailers.set("grpc-message", Utils.utf8PercentEncode(msg));
        }
      } else {
        responseTrailers.remove("grpc-message");
      }
      if (message != null) {
        return httpResponse.end(GrpcMessageImpl.encode(message));
      } else {
        return httpResponse.end();
      }
    } else {
      return httpResponse.write(GrpcMessageImpl.encode(message));
    }
  }
}

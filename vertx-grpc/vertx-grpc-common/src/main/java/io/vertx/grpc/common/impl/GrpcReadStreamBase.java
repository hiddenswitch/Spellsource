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
package io.vertx.grpc.common.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.StreamResetException;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;
import io.vertx.grpc.common.CodecException;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcReadStream;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

import static io.vertx.grpc.common.GrpcError.mapHttp2ErrorCode;

/**
 * Transforms {@code Buffer} into a stream of {@link GrpcMessage}
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class GrpcReadStreamBase<S extends GrpcReadStreamBase<S, T>, T> implements GrpcReadStream<T>, Handler<Buffer> {

  static final GrpcMessage END_SENTINEL = new GrpcMessage() {
    @Override
    public String encoding() {
      return null;
    }
    @Override
    public Buffer payload() {
      return null;
    }
  };

  protected final ContextInternal context;
  private final String encoding;
  private final ReadStream<Buffer> stream;
  private final InboundBuffer<GrpcMessage> queue;
  private Buffer buffer;
  private Handler<GrpcError> errorHandler;
  private Handler<Throwable> exceptionHandler;
  private Handler<GrpcMessage> messageHandler;
  private Handler<Void> endHandler;
  private GrpcMessage last;
  private final GrpcMessageDecoder<T> messageDecoder;
  private final Promise<Void> end;

  protected GrpcReadStreamBase(Context context, ReadStream<Buffer> stream, String encoding, GrpcMessageDecoder<T> messageDecoder) {
    this.context = (ContextInternal) context;
    this.encoding = encoding;
    this.stream = stream;
    this.queue = new InboundBuffer<>(context);
    this.messageDecoder = messageDecoder;
    this.end = ((ContextInternal) context).promise();
  }

  public void init() {
    stream.handler(this);
    stream.endHandler(v -> queue.write(END_SENTINEL));
    stream.exceptionHandler(err -> {
      if (err instanceof StreamResetException) {
        handleReset(((StreamResetException)err).getCode());
      } else {
        handleException(err);
      }
    });
    queue.drainHandler(v -> stream.resume());
    queue.handler(msg -> {
      if (msg == END_SENTINEL) {
        handleEnd();
      } else {
        handleMessage(msg);
      }
    });
  }

  protected T decodeMessage(GrpcMessage msg) throws CodecException {
    switch (msg.encoding()) {
      case "identity":
        // Nothing to do
        break;
      case "gzip": {
        msg = GrpcMessage.message("identity", GrpcMessageDecoder.GZIP.decode(msg));
        break;
      }
      default:
        throw new UnsupportedOperationException();
    }
    return messageDecoder.decode(msg);
  }

  public void handle(Buffer chunk) {
    if (buffer == null) {
      buffer = chunk;
    } else {
      buffer.appendBuffer(chunk);
    }
    int idx = 0;
    boolean pause = false;
    int len;
    while (idx + 5 <= buffer.length() && (idx + 5 + (len = buffer.getInt(idx + 1)))<= buffer.length()) {
      boolean compressed = buffer.getByte(idx) == 1;
      if (compressed && encoding == null) {
        throw new UnsupportedOperationException("Handle me");
      }
      Buffer payload = buffer.slice(idx + 5, idx + 5 + len);
      GrpcMessage message = GrpcMessage.message(compressed ? encoding : "identity", payload);
      pause |= !queue.write(message);
      idx += 5 + len;
    }
    if (pause) {
      stream.pause();
    }
    if (idx < buffer.length()) {
      buffer = buffer.getBuffer(idx, buffer.length());
    } else {
      buffer = null;
    }
  }

  public S pause() {
    queue.pause();
    return (S) this;
  }

  public S resume() {
    queue.resume();
    return (S) this;
  }

  public S fetch(long amount) {
    queue.fetch(amount);
    return (S) this;
  }

  @Override
  public S errorHandler(Handler<GrpcError> handler) {
    errorHandler = handler;
    return (S) this;
  }

  @Override
  public S exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return (S) this;
  }

  @Override
  public S messageHandler(Handler<GrpcMessage> handler) {
    messageHandler = handler;
    return (S) this;
  }

  @Override
  public S endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return (S) this;
  }

  protected void handleReset(long code) {
    Handler<GrpcError> handler = errorHandler;
    if (handler != null) {
      GrpcError error = mapHttp2ErrorCode(code);
      if (error != null) {
        handler.handle(error);
      }
    }
  }

  protected void handleException(Throwable err) {
    end.tryFail(err);
    Handler<Throwable> handler = exceptionHandler;
    if (handler != null) {
      handler.handle(err);
    }
  }

  protected void handleEnd() {
    end.tryComplete();
    Handler<Void> handler = endHandler;
    if (handler != null) {
      handler.handle(null);
    }
  }

  protected void handleMessage(GrpcMessage msg) {
    last = msg;
    Handler<GrpcMessage> handler = messageHandler;
    if (handler != null) {
      handler.handle(msg);
    }
  }

  @Override
  public Future<T> last() {
    return end()
      .map(v -> decodeMessage(last));
  }

  @Override
  public Future<Void> end() {
    return end.future();
  }

  @Override
  public <R, C> Future<R> collecting(Collector<T, C, R> collector) {
    PromiseInternal<R> promise = context.promise();
    C cumulation = collector.supplier().get();
    BiConsumer<C, T> accumulator = collector.accumulator();
    handler(elt -> accumulator.accept(cumulation, elt));
    endHandler(v -> {
      R result = collector.finisher().apply(cumulation);
      promise.tryComplete(result);
    });
    exceptionHandler(promise::tryFail);
    return promise.future();
  }
}

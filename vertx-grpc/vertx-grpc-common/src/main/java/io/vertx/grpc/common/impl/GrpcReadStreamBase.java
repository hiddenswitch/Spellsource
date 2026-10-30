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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.StreamResetException;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.concurrent.InboundMessageQueue;
import io.vertx.core.streams.ReadStream;
import io.vertx.grpc.common.*;

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
    public WireFormat format() {
      return null;
    }
    @Override
    public Buffer payload() {
      return null;
    }
  };

  protected final ContextInternal context;
  private final String encoding;
  private final WireFormat format;
  private final ReadStream<Buffer> stream;
  private final GrpcMessageDeframer deframer;
  private final InboundMessageQueue<GrpcMessage> queue;
  private Handler<Throwable> exceptionHandler;
  private Handler<GrpcMessage> messageHandler;
  private Handler<Void> endHandler;
  private Handler<InvalidMessageException> invalidMessageHandler;
  private final java.util.ArrayDeque<GrpcMessage> pendingMessages = new java.util.ArrayDeque<>(4);
  private long demand = Long.MAX_VALUE;
  private boolean endReceived;
  private boolean endDispatched;
  private Throwable failureReceived;
  private GrpcMessage last;
  private long maxMessageSize;
  private final GrpcMessageDecoder<T> messageDecoder;
  private final Promise<Void> end;
  private GrpcWriteStreamBase<?, ?> ws;

  protected GrpcReadStreamBase(Context context,
                               ReadStream<Buffer> stream,
                               String encoding,
                               WireFormat format,
                               GrpcMessageDeframer messageDeframer,
                               GrpcMessageDecoder<T> messageDecoder) {
    ContextInternal ctx = (ContextInternal) context;
    this.context = ctx;
    this.encoding = encoding;
    this.stream = stream;
    this.format = format;
    this.queue = new InboundMessageQueue<>(ctx.executor(), ctx.executor(), 8, 16) {
      @Override
      protected void handleResume() {
        stream.resume();
      }
      @Override
      protected void handlePause() {
        stream.pause();
      }
      @Override
      protected void handleMessage(GrpcMessage msg) {
        if (msg == END_SENTINEL) {
          handleEnd();
        } else {
          GrpcReadStreamBase.this.handleMessage(msg);
        }
      }
    };
    this.messageDecoder = messageDecoder;
    this.end = ctx.promise();
    this.deframer = messageDeframer;
  }

  public void init(GrpcWriteStreamBase<?, ?> ws, long maxMessageSize) {
    this.ws = ws;
    this.maxMessageSize = maxMessageSize;
    deframer.maxMessageSize(maxMessageSize);
    stream.handler(this);
    stream.endHandler(v -> {
      deframer.end();
      deframe();
      queue.write(END_SENTINEL);
    });
    stream.exceptionHandler(err -> {
      if (err instanceof StreamResetException) {
        StreamResetException reset = (StreamResetException) err;
        GrpcError error = mapHttp2ErrorCode(reset.getCode());
        if (error != null) {
          ws.handleError(error);
        }
        handleException(GrpcErrorException.create(reset));
      } else {
        handleException(err);
      }
    });
  }

  protected final T decodeMessage(GrpcMessage msg) throws CodecException {
    switch (msg.encoding()) {
      case "identity":
        // Nothing to do
        break;
      case "gzip": {
        Buffer decoded;
        try {
          decoded = Utils.gzipDecode(msg.payload(), maxMessageSize > 0 ? maxMessageSize : Integer.MAX_VALUE);
        } catch (MessageSizeOverflowException msoe) {
          Handler<InvalidMessageException> handler = invalidMessageHandler;
          if (handler != null) {
            context.dispatch(msoe, handler);
          }
          throw msoe;
        }
        msg = GrpcMessage.message("identity", msg.format(), decoded);
        break;
      }
      default:
        throw new UnsupportedOperationException();
    }
    return messageDecoder.decode(msg);
  }

  @Override
  public final WireFormat format() {
    return format;
  }

  @Override
  public final String encoding() {
    return encoding;
  }

  public final S pause() {
    synchronized (this) {
      demand = 0L;
    }
    queue.pause();
    return (S) this;
  }

  public final S resume() {
    return fetch(Long.MAX_VALUE);
  }

  public final S fetch(long amount) {
    if (amount < 0L) {
      throw new IllegalArgumentException();
    }
    synchronized (this) {
      demand += amount;
      if (demand < 0L) {
        demand = Long.MAX_VALUE;
      }
      drainPendingMessages();
    }
    queue.fetch(amount);
    return (S) this;
  }

  private void drainPendingMessages() {
    Handler<GrpcMessage> handler = messageHandler;
    if (handler != null) {
      while (demand > 0L && !pendingMessages.isEmpty()) {
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        GrpcMessage msg = pendingMessages.pollFirst();
        context.dispatch(msg, handler);
      }
      if (endReceived && pendingMessages.isEmpty() && !endDispatched && endHandler != null) {
        endDispatched = true;
        context.dispatch(endHandler);
      }
    }
  }

  @Override
  public final S exceptionHandler(Handler<Throwable> handler) {
    synchronized (this) {
      exceptionHandler = handler;
      if (handler != null && failureReceived != null) {
        context.dispatch(failureReceived, handler);
      }
    }
    return (S) this;
  }

  @Override
  public final S errorHandler(@Nullable Handler<GrpcError> handler) {
    ws.errorHandler(handler);
    return (S) this;
  }

  @Override
  public final S messageHandler(Handler<GrpcMessage> handler) {
    synchronized (this) {
      messageHandler = handler;
      drainPendingMessages();
    }
    return (S) this;
  }

  @Override
  public final S invalidMessageHandler(@Nullable Handler<InvalidMessageException> handler) {
    invalidMessageHandler = handler;
    return (S) this;
  }

  @Override
  public abstract S handler(@Nullable Handler<T> handler);

  @Override
  public final S endHandler(Handler<Void> endHandler) {
    synchronized (this) {
      this.endHandler = endHandler;
      if (endHandler != null && endReceived && pendingMessages.isEmpty() && !endDispatched) {
        endDispatched = true;
        context.dispatch(endHandler);
      }
    }
    return (S) this;
  }

  public void handle(Buffer chunk) {
    deframer.update(chunk);
    deframe();
  }

  private void deframe() {
    while (true) {
      Object ret = deframer.next();
      if (ret == null) {
        break;
      } else if (ret instanceof MessageSizeOverflowException) {
        MessageSizeOverflowException msoe = (MessageSizeOverflowException) ret;
        Handler<InvalidMessageException> handler = invalidMessageHandler;
        if (handler != null) {
          context.dispatch(msoe, handler);
        }
      } else {
        GrpcMessage msg = (GrpcMessage) ret;
        queue.write(msg);
      }
    }
  }

  public final void tryFail(Throwable err) {
    if (end.tryFail(err)) {
      synchronized (this) {
        failureReceived = err;
        Handler<Throwable> handler = exceptionHandler;
        if (handler != null) {
          context.dispatch(err, handler);
        }
      }
    }
  }

  protected final void handleException(Throwable err) {
    tryFail(err);
  }

  protected void handleEnd() {
    end.tryComplete();
    synchronized (this) {
      endReceived = true;
      if (pendingMessages.isEmpty() && !endDispatched && endHandler != null) {
        endDispatched = true;
        context.dispatch(endHandler);
      }
    }
  }

  private void handleMessage(GrpcMessage msg) {
    synchronized (this) {
      last = msg;
      Handler<GrpcMessage> handler = messageHandler;
      if (handler != null && demand > 0L && pendingMessages.isEmpty()) {
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        context.dispatch(msg, handler);
      } else {
        pendingMessages.addLast(msg);
      }
    }
  }

  @Override
  public final Future<T> last() {
    return end()
      .map(v -> {
        GrpcMessage msg;
        synchronized (this) {
          pendingMessages.clear();
          msg = last;
        }
        if (msg == null) {
          throw new GrpcErrorException(GrpcStatus.INTERNAL, "No message received before end of stream");
        }
        return decodeMessage(msg);
      });
  }

  @Override
  public Future<Void> end() {
    return end.future();
  }
}

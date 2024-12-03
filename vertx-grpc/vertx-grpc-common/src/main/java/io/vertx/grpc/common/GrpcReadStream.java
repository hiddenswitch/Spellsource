package io.vertx.grpc.common;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.streams.ReadStream;

@VertxGen
public interface GrpcReadStream<T> extends ReadStream<T> {

  /**
   * @return the {@link MultiMap} to read metadata headers
   */
  MultiMap headers();

  /**
   * @return the stream encoding, e.g {@code identity} or {@code gzip}
   */
  String encoding();

  /**
   * Set a handler to be notified with incoming encoded messages. The {@code handler} is
   * responsible for fully decoding incoming messages, including compression.
   *
   * @param handler the message handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcReadStream<T> messageHandler(@Nullable Handler<GrpcMessage> handler);

  /**
   * Set a handler to be notified with gRPC errors.
   *
   * @param handler the error handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcReadStream<T> errorHandler(@Nullable Handler<GrpcError> handler);

  @Override
  GrpcReadStream<T> exceptionHandler(@Nullable Handler<Throwable> handler);

  @Override
  GrpcReadStream<T> handler(@Nullable Handler<T> handler);

  @Override
  GrpcReadStream<T> pause();

  @Override
  GrpcReadStream<T> resume();

  @Override
  GrpcReadStream<T> fetch(long l);

  @Override
  GrpcReadStream<T> endHandler(@Nullable Handler<Void> handler);

  /**
   * @return the last element of the stream
   */
  Future<T> last();

  /**
   * @return a future signaling when the response has been fully received successfully or failed
   */
  Future<Void> end();

  /**
   * @return the result of applying a collector on the stream
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  <R, A> Future<R> collecting(java.util.stream.Collector<T , A , R> collector);

}

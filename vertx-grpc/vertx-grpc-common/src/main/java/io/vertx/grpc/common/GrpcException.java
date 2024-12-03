package io.vertx.grpc.common;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.vertx.core.http.HttpClientResponse;

public class GrpcException extends StatusRuntimeException {

  private static final long serialVersionUID = -7838327176604697641L;

  private HttpClientResponse httpResponse;

  public GrpcException(String msg, GrpcStatus status, HttpClientResponse httpResponse) {
    super(Status.fromCodeValue(status.code).withDescription(msg));
    this.httpResponse = httpResponse;
  }

  public GrpcException(StatusRuntimeException statusRuntimeException) {
    super(Status.fromThrowable(statusRuntimeException));
  }

  public GrpcException(GrpcStatus status) {
    super(Status.fromCodeValue(status.code));
  }

  public GrpcException(GrpcStatus status, Throwable err) {
    super(err instanceof StatusRuntimeException ? Status.fromThrowable(err) : err instanceof StatusException ? Status.fromThrowable(err) : Status.fromCodeValue(status.code).withCause(err));
  }

  public GrpcException(GrpcStatus status, String msg) {
    super(Status.fromCodeValue(status.code).withDescription(msg));
  }

  public GrpcStatus status() {
    return GrpcStatus.valueOf(getStatus().getCode().value());
  }

  public HttpClientResponse response() {
    return httpResponse;
  }
}

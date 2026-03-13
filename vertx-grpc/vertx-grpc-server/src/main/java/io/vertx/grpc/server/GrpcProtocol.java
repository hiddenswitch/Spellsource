package io.vertx.grpc.server;

import io.vertx.core.http.HttpVersion;

import java.util.EnumSet;

/**
 * Describe the underlying gRPC protocol.
 */
public enum GrpcProtocol {

  /**
   * gRPC over HTTP/2
   */
  HTTP_2("application/grpc", EnumSet.of(HttpVersion.HTTP_2)),

  /**
   * gRPC transcoding HTTP/1
   */
  TRANSCODING("application/json", EnumSet.allOf(HttpVersion.class)),

  /**
   * gRPC Web
   */
  WEB("application/grpc-web", EnumSet.allOf(HttpVersion.class)),

  /**
   * gRPC Web text
   */
  WEB_TEXT("application/grpc-web-text", EnumSet.allOf(HttpVersion.class));

  private final String mediaType;
  private final EnumSet<HttpVersion> acceptedVersions;

  GrpcProtocol(String mediaType, EnumSet<HttpVersion> acceptedVersions) {
    this.mediaType = mediaType;
    this.acceptedVersions = acceptedVersions;
  }

  /**
   * @return whether the protocol accepts the HTTP {@code version}
   */
  public boolean accepts(HttpVersion version) {
    return acceptedVersions.contains(version);
  }

  /**
   * @return the HTTP media type
   */
  public String mediaType() {
    return mediaType;
  }
}

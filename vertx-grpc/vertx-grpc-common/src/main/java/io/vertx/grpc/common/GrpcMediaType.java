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

package io.vertx.grpc.common;

import io.netty.util.AsciiString;
import io.vertx.codegen.annotations.Unstable;
import io.vertx.core.http.HttpHeaders;

/**
 * The gRPC media types.
 */
@Unstable
public final class GrpcMediaType {

  /**
   * gRPC.
   */
  public static final CharSequence GRPC = HttpHeaders.createOptimized("application/grpc");
  /**
   * gRPC with Protobuf message format.
   */
  public static final CharSequence GRPC_PROTO = HttpHeaders.createOptimized("application/grpc+proto");

  /**
   * gRPC Web binary.
   */
  public static final CharSequence GRPC_WEB = HttpHeaders.createOptimized("application/grpc-web");
  /**
   * gRPC Web binary with Protobuf message format.
   */
  public static final CharSequence GRPC_WEB_PROTO = HttpHeaders.createOptimized("application/grpc-web+proto");

  /**
   * Whether the provided {@code mediaType} represents gRPC-Web
   *
   * @param mediaType the value to test
   * @return {@code true} if the value represents gRPC-Web, {@code false} otherwise
   */
  public static boolean isGrpcWeb(CharSequence mediaType) {
    return AsciiString.regionMatches(GRPC_WEB, true, 0, mediaType, 0, GRPC_WEB.length());
  }

  /**
   * gRPC Web text (base64).
   */
  public static final CharSequence GRPC_WEB_TEXT = HttpHeaders.createOptimized("application/grpc-web-text");
  /**
   * gRPC Web text (base64) with Protobuf message format.
   */
  public static final CharSequence GRPC_WEB_TEXT_PROTO = HttpHeaders.createOptimized("application/grpc-web-text+proto");

  /**
   * Whether the provided {@code mediaType} represents gRPC-Web
   *
   * @param mediaType the value to test
   * @return {@code true} if the value represents gRPC-Web, {@code false} otherwise
   */
  public static boolean isGrpcWebText(CharSequence mediaType) {
    return AsciiString.regionMatches(GRPC_WEB_TEXT, true, 0, mediaType, 0, GRPC_WEB_TEXT.length());
  }

  public static WireFormat parseContentType(String contentType, String mediaType) {
    if (contentType.startsWith(mediaType)) {
      String s = contentType.substring(mediaType.length());
      WireFormat format;
      if (s.isEmpty()) {
        format = WireFormat.PROTOBUF;
      } else {
        switch (s) {
          case "+json":
            format = WireFormat.JSON;
            break;
          case "+proto":
            format = WireFormat.PROTOBUF;
            break;
          default:
            return null;
        }
      }
      return format;
    }
    return null;
  }

  private GrpcMediaType() {
    // Constants
  }
}

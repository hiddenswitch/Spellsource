/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.server.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.internal.ContextInternal;
import io.vertx.grpc.common.GrpcHeaderNames;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.server.GrpcProtocol;

public class Http2GrpcServerResponse<Req, Resp> extends GrpcServerResponseImpl<Req,Resp> {

  public Http2GrpcServerResponse(ContextInternal context, GrpcServerRequestImpl<Req, Resp> request, GrpcProtocol protocol, HttpServerResponse httpResponse, GrpcMessageEncoder<Resp> encoder) {
    super(context, request, protocol, httpResponse, encoder);
  }

  @Override
  protected void encodeGrpcHeaders(MultiMap grpcHeaders, MultiMap httpHeaders) {
    super.encodeGrpcHeaders(grpcHeaders, httpHeaders);
    httpHeaders.set(GrpcHeaderNames.GRPC_ENCODING, encoding);
    httpHeaders.set(GrpcHeaderNames.GRPC_ACCEPT_ENCODING, "gzip");
  }
}

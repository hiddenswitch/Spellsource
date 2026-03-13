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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.internal.ContextInternal;
import io.vertx.grpc.common.GrpcHeaderNames;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.WireFormat;
import io.vertx.grpc.common.impl.GrpcMethodCall;
import io.vertx.grpc.common.impl.Http2GrpcMessageDeframer;
import io.vertx.grpc.server.GrpcProtocol;

public class Http2GrpcServerRequest<Req, Resp> extends GrpcServerRequestImpl<Req, Resp> {

  public Http2GrpcServerRequest(ContextInternal context, GrpcProtocol protocol, WireFormat format, HttpServerRequest httpRequest, GrpcMessageDecoder<Req> messageDecoder, GrpcMethodCall methodCall) {
    super(context, protocol, format, httpRequest, new Http2GrpcMessageDeframer(httpRequest.headers().get(GrpcHeaderNames.GRPC_ENCODING), format), messageDecoder, methodCall);
  }
}

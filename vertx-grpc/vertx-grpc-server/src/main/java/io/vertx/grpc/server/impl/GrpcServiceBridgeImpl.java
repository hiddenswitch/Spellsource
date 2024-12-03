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

import io.grpc.Compressor;
import io.grpc.CompressorRegistry;
import io.grpc.Decompressor;
import io.grpc.DecompressorRegistry;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.impl.BridgeMessageEncoder;
import io.vertx.grpc.common.impl.BridgeMessageDecoder;
import io.vertx.grpc.common.impl.ReadStreamAdapter;
import io.vertx.grpc.common.impl.Utils;
import io.vertx.grpc.common.impl.WriteStreamAdapter;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.GrpcServerResponse;
import io.vertx.grpc.server.GrpcServiceBridge;

public class GrpcServiceBridgeImpl implements GrpcServiceBridge {

  private final ServerServiceDefinition serviceDef;

  public GrpcServiceBridgeImpl(ServerServiceDefinition serviceDef) {
    this.serviceDef = serviceDef;
  }

  @Override
  public void unbind(GrpcServer server) {
    serviceDef.getMethods().forEach(m -> unbind(server, m));
  }

  private <Req, Resp> void unbind(GrpcServer server, ServerMethodDefinition<Req, Resp> methodDef) {
    server.callHandler(methodDef.getMethodDescriptor(), null);
  }

  @Override
  public void bind(GrpcServer server) {
    serviceDef.getMethods().forEach(m -> bind(server, m));
  }

  private <Req, Resp> void bind(GrpcServer server, ServerMethodDefinition<Req, Resp> methodDef) {
    server.callHandler(methodDef.getMethodDescriptor(), req -> {
      ServerCallHandler<Req, Resp> callHandler = methodDef.getServerCallHandler();
      ServerCallImpl<Req, Resp> call = new ServerCallImpl<>(req, methodDef);
      ServerCall.Listener<Req> listener = callHandler.startCall(call, Utils.readMetadata(req.headers()));
      call.init(listener);
    });
  }

  private static class ServerCallImpl<Req, Resp> extends ServerCall<Req, Resp> {

    private final GrpcServerRequest<Req, Resp> req;
    private final ServerMethodDefinition<Req, Resp> methodDef;
    private final ReadStreamAdapter<Req> readAdapter;
    private final WriteStreamAdapter<Resp> writeAdapter;
    private ServerCall.Listener<Req> listener;
    private final Decompressor decompressor;
    private Compressor compressor;
    private boolean halfClosed;
    private boolean closed;
    private int messagesSent;

    public ServerCallImpl(GrpcServerRequest<Req, Resp> req, ServerMethodDefinition<Req, Resp> methodDef) {

      String encoding = req.encoding();



      this.decompressor = DecompressorRegistry.getDefaultInstance().lookupDecompressor(encoding);
      this.req = req;
      this.methodDef = methodDef;
      this.readAdapter = new ReadStreamAdapter<Req>() {
        @Override
        protected void handleClose() {
          halfClosed = true;
          listener.onHalfClose();
        }
        @Override
        protected void handleMessage(Req msg) {
          listener.onMessage(msg);
        }
      };
      this.writeAdapter = new WriteStreamAdapter<Resp>() {
        @Override
        protected void handleReady() {
          listener.onReady();
        }
      };
    }

    void init(ServerCall.Listener<Req> listener) {
      this.listener = listener;
      req.errorHandler(error -> {
        if (error == GrpcError.CANCELLED && !closed) {
          listener.onCancel();
        }
      });
      readAdapter.init(req, new BridgeMessageDecoder<>(methodDef.getMethodDescriptor().getRequestMarshaller(), decompressor));
      writeAdapter.init(req.response(), new BridgeMessageEncoder<>(methodDef.getMethodDescriptor().getResponseMarshaller(), compressor));
    }

    @Override
    public boolean isReady() {
      return writeAdapter.isReady();
    }

    @Override
    public void request(int numMessages) {
      readAdapter.request(numMessages);
    }

    @Override
    public void sendHeaders(Metadata headers) {
      Utils.writeMetadata(headers, req.response().headers());
    }

    @Override
    public void sendMessage(Resp message) {
      messagesSent++;
      writeAdapter.write(message);
    }

    @Override
    public void close(Status status, Metadata trailers) {
      if (closed) {
        throw new IllegalStateException("Already closed");
      }
      closed = true;
      GrpcServerResponse<Req, Resp> response = req.response();
      if (status == Status.OK && methodDef.getMethodDescriptor().getType().serverSendsOneMessage() && messagesSent == 0) {
        response.status(GrpcStatus.UNAVAILABLE).end();
      } else {
        Utils.writeMetadata(trailers, response.trailers());
        response.status(GrpcStatus.valueOf(status.getCode().value()));
        response.statusMessage(status.getDescription());
        response.end();
      }
      listener.onComplete();
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public MethodDescriptor<Req, Resp> getMethodDescriptor() {
      return methodDef.getMethodDescriptor();
    }

    @Override
    public void setCompression(String encoding) {
      compressor = CompressorRegistry.getDefaultInstance().lookupCompressor(encoding);
      req.response().encoding(encoding);
    }

    @Override
    public void setMessageCompression(boolean enabled) {
      // ????
      super.setMessageCompression(enabled);
    }
  }
}

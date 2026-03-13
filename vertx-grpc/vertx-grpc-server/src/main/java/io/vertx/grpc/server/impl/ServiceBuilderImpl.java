package io.vertx.grpc.server.impl;

import com.google.protobuf.Descriptors;
import io.vertx.core.Handler;
import io.vertx.grpc.common.ServiceMethod;
import io.vertx.grpc.common.ServiceName;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.Service;
import io.vertx.grpc.server.ServiceBuilder;

import java.util.LinkedList;
import java.util.List;

public class ServiceBuilderImpl implements ServiceBuilder {

  private final ServiceName serviceName;

  private final Descriptors.ServiceDescriptor descriptor;
  private final List<ServiceMethodBinding<?, ?>> handlers = new LinkedList<>(); // Maybe use MAP instead

  public ServiceBuilderImpl(ServiceName serviceName, Descriptors.ServiceDescriptor descriptor) {
    this.serviceName = serviceName;
    this.descriptor = descriptor;
  }

  @Override
  public <Req, Resp> ServiceBuilder bind(ServiceMethod<Req, Resp> serviceMethod, Handler<GrpcServerRequest<Req, Resp>> handler) {
    handlers.add(new ServiceMethodBinding<>(serviceMethod, handler));
    return this;
  }

  @Override
  public Service build() {
    return new Service() {
      @Override
      public ServiceName name() {
        return serviceName;
      }

      @Override
      public Descriptors.ServiceDescriptor descriptor() {
        return descriptor;
      }
      @Override
      public void bind(GrpcServer server) {
        handlers.forEach(h -> h.bind(server));
      }
    };
  }

  /**
   * Contains data about a service method and its associated request handler.
   * This class is used internally to store the mapping between a gRPC service method
   * and the handler that processes requests for that method.
   *
   * @param <Req> the request type for the service method
   * @param <Resp> the response type for the service method
   */
  public static final class ServiceMethodBinding<Req, Resp> {
    private final ServiceMethod<Req, Resp> serviceMethod;
    private final Handler<GrpcServerRequest<Req, Resp>> handler;

    public ServiceMethodBinding(ServiceMethod<Req, Resp> serviceMethod, Handler<GrpcServerRequest<Req, Resp>> handler) {
      this.serviceMethod = serviceMethod;
      this.handler = handler;
    }

    /**
     * Gets the service method.
     *
     * @return the service method
     */
    private ServiceMethod<Req, Resp> serviceMethod() {
      return serviceMethod;
    }

    /**
     * Gets the handler for the service method.
     *
     * @return the handler that processes requests for the service method
     */
    private Handler<GrpcServerRequest<Req, Resp>> handler() {
      return handler;
    }

    /**
     * Binds this service method and its handler to the specified gRPC server.
     * This registers the handler with the server so that it can process requests
     * for the service method.
     *
     * @param server the gRPC server to bind to
     */
    public void bind(GrpcServer server) {
      server.callHandler(serviceMethod, handler);
    }
  }
}

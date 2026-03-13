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
package io.vertx.grpc.client;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.AddressResolver;
import io.vertx.core.net.endpoint.LoadBalancer;

/**
 * A builder for {@link GrpcClient}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface GrpcClientBuilder<C> {

  /**
   * Configure the client options.
   * @param options the client options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcClientBuilder<C> with(GrpcClientOptions options);

  /**
   * Configure the client HTTP transport options.
   * @param transportOptions the client transport options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcClientBuilder<C> with(HttpClientOptions transportOptions);

  /**
   * Configure the client to use a specific address resolver.
   *
   * @param resolver the address resolver
   */
  @GenIgnore({"permitted-type"})
  GrpcClientBuilder<C> withAddressResolver(AddressResolver resolver);

  /**
   * Configure the client to use a load balancer.
   *
   * @param loadBalancer the load balancer
   */
  @GenIgnore({"permitted-type"})
  GrpcClientBuilder<C> withLoadBalancer(LoadBalancer loadBalancer);

  /**
   * Build and return the client.
   * @return the client as configured by this builder
   */
  C build();

}

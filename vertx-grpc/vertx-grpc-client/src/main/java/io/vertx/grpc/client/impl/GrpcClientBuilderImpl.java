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
package io.vertx.grpc.client.impl;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientBuilder;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.AddressResolver;
import io.vertx.core.net.endpoint.LoadBalancer;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientBuilder;
import io.vertx.grpc.client.GrpcClientOptions;

/**
 * Implementation of {@link GrpcClientBuilder}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientBuilderImpl<C extends GrpcClient> implements GrpcClientBuilder<C> {

  private final Vertx vertx;
  private GrpcClientOptions options;
  private HttpClientOptions transportOptions;
  private AddressResolver addressResolver;
  private LoadBalancer loadBalancer;

  public GrpcClientBuilderImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public GrpcClientBuilderImpl<C> with(GrpcClientOptions options) {
    this.options = options == null ? null : new GrpcClientOptions(options);
    return this;
  }

  @Override
  public GrpcClientBuilderImpl<C> with(HttpClientOptions transportOptions) {
    this.transportOptions = transportOptions == null ? null : new HttpClientOptions(transportOptions);
    return this;
  }

  @Override
  public GrpcClientBuilderImpl<C> withAddressResolver(AddressResolver resolver) {
    this.addressResolver = resolver;
    return this;
  }

  @Override
  public GrpcClientBuilderImpl<C> withLoadBalancer(LoadBalancer loadBalancer) {
    this.loadBalancer = loadBalancer;
    return this;
  }

  @Override
  public C build() {
    HttpClientOptions transportOptions = this.transportOptions;
    if (transportOptions == null) {
      transportOptions = new HttpClientOptions().setHttp2ClearTextUpgrade(false);
    }
    transportOptions = transportOptions.setProtocolVersion(HttpVersion.HTTP_2);
    HttpClientBuilder transportBuilder = vertx
      .httpClientBuilder()
      .with(transportOptions);
    if (loadBalancer != null) {
      transportBuilder.withLoadBalancer(loadBalancer);
    }
    if (addressResolver != null) {
      transportBuilder.withAddressResolver(addressResolver);
    }
    GrpcClientOptions options = this.options;
    if (options == null) {
      options = new GrpcClientOptions();
    }
    return create(vertx, options, transportBuilder.build());
  }

  protected C create(Vertx vertx, GrpcClientOptions options, HttpClient transport) {
    GrpcClient client = new GrpcClientImpl(vertx, options, transport, true);
    return (C) client;
  }
}

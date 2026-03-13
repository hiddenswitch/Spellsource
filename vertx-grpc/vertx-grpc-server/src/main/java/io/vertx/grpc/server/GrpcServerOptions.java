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
package io.vertx.grpc.server;

import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration for a {@link GrpcServer}.
 */
public class GrpcServerOptions {

  /**
   *
   */
  public static final Set<GrpcProtocol> DEFAULT_ENABLED_PROTOCOLS = Collections.unmodifiableSet(EnumSet.allOf(GrpcProtocol.class));

  /**
   * Whether the server schedule deadline automatically when a request carrying a timeout is received, by default = {@code false}
   */
  public static final boolean DEFAULT_SCHEDULE_DEADLINE_AUTOMATICALLY = false;

  /**
   * Whether the server propagates a deadline, by default = {@code false}
   */
  public static final boolean DEFAULT_PROPAGATE_DEADLINE = false;

  /**
   * The default maximum message size in bytes accepted from a client = {@code 256KB}
   */
  public static final long DEFAULT_MAX_MESSAGE_SIZE = 256 * 1024;

  private Set<GrpcProtocol> enabledProtocols;
  private boolean scheduleDeadlineAutomatically;
  private boolean deadlinePropagation;
  private long maxMessageSize;

  /**
   * Default options.
   */
  public GrpcServerOptions() {
    enabledProtocols = EnumSet.copyOf(DEFAULT_ENABLED_PROTOCOLS);
    scheduleDeadlineAutomatically = DEFAULT_SCHEDULE_DEADLINE_AUTOMATICALLY;
    deadlinePropagation = DEFAULT_PROPAGATE_DEADLINE;
    maxMessageSize = DEFAULT_MAX_MESSAGE_SIZE;
  }

  /**
   * Copy constructor.
   */
  public GrpcServerOptions(GrpcServerOptions other) {
    enabledProtocols = EnumSet.copyOf(other.enabledProtocols);
    scheduleDeadlineAutomatically = other.scheduleDeadlineAutomatically;
    deadlinePropagation = other.deadlinePropagation;
    maxMessageSize = other.maxMessageSize;
  }

  /**
   * Creates options from JSON.
   */
  public GrpcServerOptions(JsonObject json) {
    this();
    // JSON conversion not implemented in fork
  }

  public boolean isProtocolEnabled(GrpcProtocol protocol) {
    return enabledProtocols.contains(protocol);
  }

  public GrpcServerOptions addEnabledProtocol(GrpcProtocol protocol) {
    enabledProtocols.add(protocol);
    return this;
  }

  public GrpcServerOptions removeEnabledProtocol(GrpcProtocol protocol) {
    enabledProtocols.remove(protocol);
    return this;
  }

  public Set<GrpcProtocol> getEnabledProtocols() {
    return enabledProtocols;
  }

  /**
   * @return whether the server will automatically schedule a deadline when a request carrying a timeout is received.
   */
  public boolean getScheduleDeadlineAutomatically() {
    return scheduleDeadlineAutomatically;
  }

  /**
   * <p>Set whether a deadline is automatically scheduled when a request carrying a timeout is received.</p>
   * <ul>
   * <li>When a deadline is automatically scheduled and a request carrying a timeout is received, a deadline (timer)
   * will be created to cancel the request when the response has not been timely sent. The deadline can be obtained
   * with {@link GrpcServerRequest#deadline()}.</li>
   * <li>When the deadline is not set and a request carrying a timeout is received, the timeout is available with {@link GrpcServerRequest#timeout()}
   * and it is the responsibility of the service to eventually cancel the request. Note: the client might cancel the request as well when its local
   * deadline is met.</li>
   * </ul>
   *
   * @param scheduleDeadlineAutomatically whether to schedule a deadline automatically
   * @return a reference to this, so the API can be used fluently
   */
  public GrpcServerOptions setScheduleDeadlineAutomatically(boolean scheduleDeadlineAutomatically) {
    this.scheduleDeadlineAutomatically = scheduleDeadlineAutomatically;
    return this;
  }

  /**
   * @return whether the server propagate deadlines to {@code io.vertx.grpc.client.GrpcClientRequest}.
   */
  public boolean getDeadlinePropagation() {
    return deadlinePropagation;
  }

  /**
   * Set whether the server propagate deadlines to {@code io.vertx.grpc.client.GrpcClientRequest}.
   *
   * @param deadlinePropagation the propagation setting
   * @return a reference to this, so the API can be used fluently
   */
  public GrpcServerOptions setDeadlinePropagation(boolean deadlinePropagation) {
    this.deadlinePropagation = deadlinePropagation;
    return this;
  }


  /**
   * @return the maximum message size in bytes accepted by the server
   */
  public long getMaxMessageSize() {
    return maxMessageSize;
  }

  /**
   * Set the maximum message size in bytes accepted from a client, the maximum value is {@code 0xFFFFFFFF}
   * @param maxMessageSize the size
   * @return a reference to this, so the API can be used fluently
   */
  public GrpcServerOptions setMaxMessageSize(long maxMessageSize) {
    if (maxMessageSize <= 0) {
      throw new IllegalArgumentException("Max message size must be > 0");
    }
    if (maxMessageSize > 0xFFFFFFFFL) {
      throw new IllegalArgumentException("Max message size must be <= 0xFFFFFFFF");
    }
    this.maxMessageSize = maxMessageSize;
    return this;
  }

  /**
   * @return a JSON representation of options
   */
  public JsonObject toJson() {
    return new JsonObject();
  }

  @Override
  public String toString() {
    return toJson().encode();
  }
}

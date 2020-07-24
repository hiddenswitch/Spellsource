package io.vertx.ext.sync.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.ReceivePort;
import io.vertx.core.VertxException;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlerReceiverAdaptorImpl<T> implements HandlerReceiverAdaptor<T> {

  private final FiberScheduler fiberScheduler;
  private final Channel<T> channel;

  public HandlerReceiverAdaptorImpl(FiberScheduler fiberScheduler) {
    this.fiberScheduler = fiberScheduler;
    channel = Channels.newChannel(-1, Channels.OverflowPolicy.DROP, true, true);
  }

  public HandlerReceiverAdaptorImpl(FiberScheduler fiberScheduler, Channel<T> channel) {
    this.fiberScheduler = fiberScheduler;
    this.channel = channel;
  }

  @Override
  @Suspendable
  public void handle(T t) {
	  new Fiber<Void>(Strand.currentStrand().getName() + "ephemeral-sender", fiberScheduler, Sync.DEFAULT_STACK_SIZE, () -> {
      try {
        channel.send(t);
      } catch (Exception e) {
        throw new VertxException(e);
      }
    }).start();
  }

  // Access to the underlying Quasar receivePort
  public ReceivePort<T> receivePort() {
    return channel;
  }

  @Suspendable
  public T receive() {
    try {
      return channel.receive();
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }


  @Suspendable
  public T receive(long timeout) {
    try {
      return channel.receive(timeout, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }
}

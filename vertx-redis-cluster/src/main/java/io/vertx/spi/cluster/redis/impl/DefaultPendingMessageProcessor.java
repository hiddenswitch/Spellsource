/*
 * Copyright (c) 2019 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.spi.cluster.redis.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryContext;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.EventBusImpl;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import io.vertx.core.eventbus.impl.clustered.ClusteredMessage;
import io.vertx.core.net.impl.ServerID;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.spi.cluster.redis.Factory.PendingMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @see io.vertx.core.eventbus.impl.clustered.ConnectionHolder
 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus
 */
class DefaultPendingMessageProcessor implements PendingMessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(DefaultPendingMessageProcessor.class);

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ConnectionHolder
	 */
	private static final String PING_ADDRESS = "__vertx_ping";

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus#generateReplyAddress
	 */
	private static final String GENERATED_REPLY_ADDRESS_PREFIX = "__vertx.reply.";

	private final String retryHeaderKey = "__retry_outbound_interceptor__";
	private final int failureCode = -2;

	private Vertx vertx;
	@SuppressWarnings("unused")
	private final ClusterManager clusterManager;
	private final ClusteredEventBus eventBus;
	private final EventBusMetrics<?> metrics;

	private final AsyncMultiMap<String, ClusterNodeInfo> subs;

	public DefaultPendingMessageProcessor(Vertx vertx, ClusterManager clusterManager,
	                                      AsyncMultiMap<String, ClusterNodeInfo> subs) {
		this.vertx = vertx;
		this.clusterManager = clusterManager;
		this.eventBus = (ClusteredEventBus) vertx.eventBus();
		this.subs = subs;
		this.metrics = Utility.Reflection.getFinalField(eventBus, EventBusImpl.class, "metrics");
	}

	@Override
	public void run() {
		log.debug("...");
		eventBus.addOutboundInterceptor(ctx -> { // sendInterceptors (sendReply)
			Message<?> message = ctx.message();
			if (ctx.send() && message instanceof ClusteredMessage) {
				ClusteredMessage<?, ?> msg = (ClusteredMessage<?, ?>) message;
				boolean fromRetry = msg.headers().get(retryHeaderKey) != null;
				if (!msg.isFromWire() && !PING_ADDRESS.equals(msg.address())
						&& !msg.address().startsWith(GENERATED_REPLY_ADDRESS_PREFIX)
						&& !(msg.writeHandler() instanceof PendingWriteHandler) && !fromRetry) {
					PendingWriteHandler pendingWriteHandler = new PendingWriteHandler(vertx, ctx, msg);
					Utility.Reflection.setField(msg, MessageImpl.class, "writeHandler", pendingWriteHandler);
				}
				ctx.next();
			} else {
				ctx.next();
			}
		});
	}

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ConnectionHolder
	 */
	public class PendingWriteHandler implements Handler<AsyncResult<Void>> {

		private final Vertx vertx;
		@SuppressWarnings("unused")
		private final DeliveryContext<?> ctx;
		private final ClusteredMessage<?, ?> msg;
		private final Handler<AsyncResult<Void>> wrapWriteHandler;

		public PendingWriteHandler(Vertx vertx, DeliveryContext<?> ctx, ClusteredMessage<?, ?> msg) {
			this.vertx = vertx;
			this.ctx = ctx;
			this.msg = msg;
			this.wrapWriteHandler = msg.writeHandler();
		}

		@Override
		public void handle(AsyncResult<Void> ar) {
			if (wrapWriteHandler == null) {
				if (ar.failed()) {
					if (isConnectionRefusedErr(ar.cause())) {
						resend(vertx, msg, ar.cause());
					}
				}
			} else {
				if (ar.succeeded()) {
					wrapWriteHandler.handle(Future.succeededFuture(ar.result()));
				} else {
					if (isConnectionRefusedErr(ar.cause())) {
						wrapWriteHandler.handle(Future.failedFuture(ar.cause()));
						// resend(vertx, msg, ar.cause()); // FIXME: needed ?
					} else {
						wrapWriteHandler.handle(Future.failedFuture(ar.cause()));
					}
				}
			}
		}
	}

	/**
	 * FIXME
	 */
	private void resend(Vertx vertx, ClusteredMessage<?, ?> msg, Throwable err) {
		ServerID sender = getSender(msg);
		ServerID receiver = getReceiver(err);
		msg.headers().set(retryHeaderKey, msg.address());

		if (receiver == null) {
			log.info("(receiver == null): {}, sender: {}, failed receiver: {}, address: {}, replyAddress: {}", sender,
					receiver, msg.address(), msg.replyAddress());
			msg.fail(failureCode, err.getMessage()); // Reply
			return;
		}
		subs.get(msg.address(), ar -> {
			if (ar.succeeded()) {
				ChoosableIterable<ClusterNodeInfo> serverIDs = ar.result();
				if (serverIDs != null && !serverIDs.isEmpty()) {
					// Choose one
					ServerID sid = chooseNewOne(sender, receiver, serverIDs);
					if (sid != null) {
//            log.info("Choose new receiver: {}, sender: {}, failed receiver: {}, address: {}, replyAddress: {}",
//                sid, sender, receiver, msg.address(), msg.replyAddress());
						sendRemote(sid, msg);
					} else {
						log.debug("Choose another failure: sid: {}, sender: {}, failed receiver: {}, address: {}, replyAddress: {}", sid,
								sender, receiver, msg.address(), msg.replyAddress());
						msg.fail(failureCode, err.getMessage()); // Reply
					}
				} else {
					log.debug("Choose another failure: serverIDs: {}, sender: {}, failed receiver: {}, address: {}, replyAddress: {}",
							serverIDs, sender, receiver, msg.address(), msg.replyAddress());
					msg.fail(failureCode, err.getMessage()); // Reply
				}
			} else {
				log.debug("Choose another failure: error: {}, sender: {}, failed receiver: {}, address: {}, replyAddress: {}",
						ar.cause().getMessage(), sender, receiver, msg.address(), msg.replyAddress());
				msg.fail(failureCode, err.getMessage()); // Reply
			}
		});
	}

	private ServerID chooseNewOne(ServerID sender, ServerID receiver, ChoosableIterable<ClusterNodeInfo> serverIDs) {
		ServerID entry = null;
		boolean foundSender = false;
		boolean foundReceiver = false;
		ServerID newReceiver = null;
		int loop = 0;
		while (true) {
			ClusterNodeInfo ci = serverIDs.choose();
			ServerID sid = ci == null ? null : ci.serverID;
			if (sid == null) {
				break;
			}

			if (entry == null) {
				entry = sid;
			} else if (sid.equals(entry)) {
				break;
			}

			if (sid.equals(sender)) {
				if (foundSender) {
					break;
				} else {
					foundSender = true;
				}
			} else if (sid.equals(receiver)) {
				if (foundReceiver) {
					break;
				} else {
					foundReceiver = true;
				}
			} else {
				newReceiver = sid;
				break;
			}

			if (++loop > 3) { // MAX: 3
				break;
			}
		}
		return newReceiver;
	}

	private void sendRemote(ServerID remoteServerID, ClusteredMessage<?, ?> msg) {
		if (metrics != null) {
			metrics.messageSent(msg.address(), false, false, true);
		}
		Utility.Reflection.invokeMethod(eventBus, ClusteredEventBus.class, "sendRemote",
				new Class[]{ServerID.class, MessageImpl.class}, new Object[]{remoteServerID, msg});
	}

	private ServerID getSender(ClusteredMessage<?, ?> msg) {
		ServerID sender = Utility.Reflection.invokeMethod(msg, ClusteredMessage.class, "getSender");
		return sender;
	}

	private ServerID getReceiver(Throwable e) {
		final String matchedPrefix = "Connection refused:";
		ServerID receiver = null;
		while (e != null && receiver == null) {
			if (e instanceof InvocationTargetException) {
				e = ((InvocationTargetException) e).getCause();
			}
			String errMsg = e.getMessage();
			if (errMsg != null && errMsg.startsWith(matchedPrefix)) { // Connection refused: /192.168.99.1:18081
				String addr = errMsg.substring(matchedPrefix.length()).trim();
				if (addr.startsWith("/")) {
					addr = addr.substring(1);
				}
				int idx = addr.indexOf(':');
				String host = null;
				int port = -1;
				if (idx != -1) {
					host = addr.substring(0, idx);
					try {
						port = Integer.parseInt(addr.substring(idx + 1));
					} catch (NumberFormatException ex) {
						log.debug("errMsg: {}, parse port failed: {}", errMsg, ex.toString());
						break;
					}
				}
				if (host != null && port != -1) {
					receiver = new ServerID(port, host);
					break;
				}
			}
			e = e.getCause();
		}
		return receiver;
	}

	private boolean isConnectionRefusedErr(Throwable e) {
		final String matchedPrefix = "Connection refused:";
		boolean connectionRefusedErr = false;
		while (e != null && !connectionRefusedErr) {
			if (e instanceof InvocationTargetException) {
				e = ((InvocationTargetException) e).getCause();
			}
			String errMsg = e.getMessage();
			if (errMsg != null && errMsg.startsWith(matchedPrefix)) { // Connection refused: /192.168.99.1:18081
				connectionRefusedErr = true;
				break;
			}
			e = e.getCause();
		}
		return connectionRefusedErr;
	}

}

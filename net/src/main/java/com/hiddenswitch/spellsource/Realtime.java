package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.SuspendablePump;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.*;
import io.reactivex.disposables.Disposable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import static io.vertx.ext.sync.Sync.awaitResult;


public class Realtime {
	private static EnvelopeMap methods = new EnvelopeMap();
	private static EnvelopeMap publishers = new EnvelopeMap();
	private static Map<Class, Function> adders = new ConcurrentHashMap<>();

	static <T> void method(Function<EnvelopeMethod, T> unpacker, SuspendableAction1<MethodContext<T>> handler) {
		methods.put(unpacker, handler);
	}

	static <TReq, TRes> void publish(Class<? extends TReq> reqClass, Function<EnvelopeSub, TReq> unpacker, Function<TRes, EnvelopeAdded> mailer, SuspendableAction1<SubscriptionContext<TReq, TRes>> handler) {
		publishers.put(unpacker, handler);
		adders.put(reqClass, mailer);
	}

	public static Handler<RoutingContext> create() {
		return Sync.suspendableHandler((routingContext) -> {
			String userId = Accounts.userId(routingContext);
			if (userId == null) {
				throw new SecurityException("Not authorized");
			}

			final Vertx vertx = Vertx.currentContext().owner();

			try {
				final Lock lock = awaitResult(h -> vertx.sharedData().getLockWithTimeout("bridge-lock-" + userId, 400L, h));
				final EventBus bus = vertx.eventBus();
				final ServerWebSocket socket = routingContext.request().upgrade();
				final MessageConsumer<Buffer> consumer = bus.<Buffer>consumer("bridge-" + userId);
				final Pump toUser = new SuspendablePump<>(consumer.bodyStream(), socket, Integer.MAX_VALUE).start();
				final Deque<Pump> pumps = new ConcurrentLinkedDeque<>();
				pumps.add(toUser);
				final Set<SubscriptionContext> subs = new ConcurrentHashSet<>();

				socket.closeHandler(ignored -> {
					consumer.unregister();
					for (Pump pump : pumps) {
						try {
							pump.stop();
						} catch (IllegalStateException ignoredException) {
						}
					}

					for (SubscriptionContext sub : subs) {
						sub.close();
					}

					lock.release();
				});

				socket.handler(Sync.suspendableHandler(buffer -> {
					Envelope envelope = Json.decodeValue(buffer, Envelope.class);

					if (envelope.getSub() != null) {
						// Subscription to data

						for (Map.Entry<Function, SuspendableAction1> publisher : publishers.entries()) {
							Function<EnvelopeSub, ?> func = (Function<EnvelopeSub, ?>) publisher.getKey();
							final Object request = func.apply(envelope.getSub());
							if (request != null) {
								// Match
								Class requestClass = request.getClass();
								SuspendableAction1<SubscriptionContext<?, ?>> handler = (SuspendableAction1<SubscriptionContext<?, ?>>) publisher.getValue();
								final SubscriptionContext sub = new SubscriptionContext() {
									private Deque<Disposable> disposables = new ConcurrentLinkedDeque<>();

									@Override
									public UserId user() {
										return new UserId(userId);
									}

									@Override
									public Object request() {
										return request;
									}

									@Override
									public DiffContext<Object, Comparable<String>> client() {
										return new DiffContext<Object, Comparable<String>>() {
											@Override
											public void removed(Comparable<String> id) {

											}

											@Override
											public void addedBefore(Comparable<String> newDocId, Object newDoc, @Nullable Comparable<String> beforeId) {

											}

											@Override
											public void added(Comparable<String> newDocId, Object newDoc) {
												EnvelopeAdded message = (EnvelopeAdded) adders.get(requestClass).apply(newDoc);
												socket.write(Json.encodeToBuffer(new Envelope().added(message)));
											}

											@Override
											public void possiblyChanged(Comparable<String> newDocId, Object oldDoc, Object newDoc) {

											}

											@Override
											public void movedBefore(Comparable<String> id, @Nullable Comparable<String> beforeId) {

											}

											@Override
											public Function<Object, Comparable<String>> getKeyer() {
												return null;
											}
										};
									}

									@Override
									public void close() {
										for (Disposable disposable : disposables) {
											disposable.dispose();
										}
									}

									@Override
									public void addDisposable(Disposable disposable) {
										this.disposables.add(disposable);
									}
								};
								subs.add(sub);
								handler.call(sub);
								break;
							}
						}
					}

					if (envelope.getMethod() != null) {
						EnvelopeResult result = new EnvelopeResult();
						// Method call
						for (Map.Entry<Function, SuspendableAction1> method : methods.entries()) {
							Function<EnvelopeMethod, ?> func = (Function<EnvelopeMethod, ?>) method.getKey();
							final Object request = func.apply(envelope.getMethod());
							if (request != null) {
								SuspendableAction1<MethodContext<?>> handler = (SuspendableAction1<MethodContext<?>>) method.getValue();
								handler.call(new MethodContext() {
									@Override
									public UserId user() {
										return new UserId(userId);
									}

									@Override
									public EnvelopeResult result() {
										return result;
									}

									@Override
									public Object request() {
										return request;
									}
								});
								break;
							}
						}
						socket.write(Json.encodeToBuffer(new Envelope().result(result)));
					}
				}));

			} catch (VertxException ex) {
				throw new RuntimeException("Already connected or timed out.");
			}
		});
	}
}
package com.hiddenswitch.framework.impl;

import com.google.protobuf.Message;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Set;

public class CodecRegistration {

	private final static WeakVertxMap<Set<Object>> registrations = new WeakVertxMap<>(vertx -> new ConcurrentHashSet<>());

	public static <T> CodecRegistration register(Class<T> target) {
		return new CodecRegistration().andRegister(target);
	}

	public static <T> CodecRegistration register(Message target) {
		return new CodecRegistration().andRegister(target);
	}

	public <T> CodecRegistration andRegister(Class<T> target) {
		synchronized (registrations) {
			var objects = registrations.get();
			if (objects.contains(target)) {
				return this;
			}
			objects.add(target);
			Vertx.currentContext().owner().eventBus().registerDefaultCodec(target, new JsonMessageCodec<>(target));
			return this;
		}
	}

	public <T> CodecRegistration andRegister(Message target) {
		synchronized (registrations) {
			var objects = registrations.get();
			if (objects.contains(target)) {
				return this;
			}
			objects.add(target);
			var messageProtobufCodec = new ProtobufCodec<>(target);
			Vertx.currentContext().owner().eventBus().registerDefaultCodec(messageProtobufCodec.getTargetClass(), messageProtobufCodec);
			return this;
		}
	}
}
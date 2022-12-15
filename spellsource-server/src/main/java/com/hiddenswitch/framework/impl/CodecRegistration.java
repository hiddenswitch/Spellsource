package com.hiddenswitch.framework.impl;

import com.google.protobuf.Message;
import io.vertx.core.Vertx;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CodecRegistration {
	private static final Lock lock = new ReentrantLock();
	private final static WeakVertxMap<Set<Object>> registrations = new WeakVertxMap<>(vertx -> new CopyOnWriteArraySet<>());

	public static <T> CodecRegistration register(Class<T> target) {
		return new CodecRegistration().andRegister(target);
	}

	public static <T> CodecRegistration register(Message target) {
		return new CodecRegistration().andRegister(target);
	}

	public <T> CodecRegistration andRegister(Class<T> target) {
		lock.lock();
		try {
			var objects = registrations.get();
			if (objects.contains(target)) {
				return this;
			}
			objects.add(target);
			Vertx.currentContext().owner().eventBus().registerDefaultCodec(target, new JsonMessageCodec<>(target));
			return this;
		} finally {
			lock.unlock();
		}
	}

	public <T> CodecRegistration andRegister(Message target) {
		lock.lock();
		try {
			var objects = registrations.get();
			if (objects.contains(target)) {
				return this;
			}
			objects.add(target);
			var messageProtobufCodec = new ProtobufCodec<>(target);
			Vertx.currentContext().owner().eventBus().registerDefaultCodec(messageProtobufCodec.getTargetClass(), messageProtobufCodec);
			return this;
		} finally {
			lock.unlock();
		}
	}
}
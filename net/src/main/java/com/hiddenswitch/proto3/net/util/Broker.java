package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.sync.Sync;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by bberman on 12/7/16.
 */
public class Broker {
	@Suspendable
	public static <T, R extends T> void of(R instance, Class<T> serviceInterface, final EventBus eb) {
		final String name = serviceInterface.getName();

		for (Method method : serviceInterface.getDeclaredMethods()) {
			String methodName = name + "::" + method.getName();

			eb.consumer(methodName, Sync.fiberHandler(Consumer.of(arg -> {
				try {
					return method.invoke(instance, arg);
				} catch (InvocationTargetException e) {
					RuntimeException re = (RuntimeException)(e.getTargetException());
					if (re != null) {
						throw re;
					}
					return null;
				} catch (IllegalAccessException e) {
					return null;
				} catch (Throwable e) {
					throw e;
				}
			})));
		}
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	public static <T> ServiceProxy<T> proxy(Class<? extends T> serviceInterface, final EventBus bus) {
		final VertxInvocationHandler<T> invocationHandler = new VertxInvocationHandler<>();

		ServiceProxy<T> result = new ServiceProxy<>((T) Proxy.newProxyInstance(
				serviceInterface.getClassLoader(),
				new Class[]{serviceInterface},
				invocationHandler
		));

		invocationHandler.eb = bus;
		invocationHandler.name = serviceInterface.getName();
		invocationHandler.serviceProxy = result;

		return result;
	}

}

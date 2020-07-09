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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import io.vertx.core.impl.HAManager;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.spi.cluster.ClusterManager;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Non Public API Utility
 *
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 */
class Utility {

	static ClusteredEventBus eventBus(Vertx vertx) {
		return (ClusteredEventBus) vertx.eventBus();
	}

	public static ClusterManager clusterManager(Vertx vertx) {
		return Reflection.getFinalField(eventBus(vertx), ClusteredEventBus.class, "clusterManager");
	}

	static HAManager haManager(Vertx vertx) {
		return Reflection.getFinalField(vertx, VertxImpl.class, "haManager");
	}

	static Map<String, String> clusterMap(HAManager haManager) {
		return Reflection.getFinalField(haManager, HAManager.class, "clusterMap");
	}

	public static Map<String, String> clusterMap(Vertx vertx) {
		return clusterMap(haManager(vertx));
	}

	public static class Reflection {

		/**
		 * @param reflectObj null for static field
		 */
		@SuppressWarnings("unchecked")
		public static <T> T getFinalField(Object reflectObj, Class<?> clsObj, String fieldName) {
    	/*
      Objects.requireNonNull(clsObj, "clsObj");
      Objects.requireNonNull(fieldName, "fieldName");
      try {
        Field field = clsObj.getDeclaredField(fieldName);
        boolean keepStatus = field.isAccessible();
        if (!keepStatus) {
          field.setAccessible(true);
        }
        try {
          Field modifiersField = Field.class.getDeclaredField("modifiers");
          modifiersField.setAccessible(true);
          modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
          //
          return (T) field.get(reflectObj);
        } finally {
          field.setAccessible(keepStatus);
        }
      } catch (Exception e) {
        Throwable t = e.getCause() != null && e instanceof InvocationTargetException ? e.getCause() : e;
        throw new RuntimeException(fieldName, t);
      }*/
			try {
				return (T) FieldUtils.readField(reflectObj, fieldName, true);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(fieldName, e);
			}
		}

		/**
		 * @param reflectObj null for static field
		 */
		static public void setFinalField(Object reflectObj, Class<?> clsObj, String fieldName, Object newValue) {
			Objects.requireNonNull(clsObj, "clsObj");
			Objects.requireNonNull(fieldName, "fieldName");
			try {
				Field field = clsObj.getDeclaredField(fieldName);
				boolean keepStatus = field.isAccessible();
				if (!keepStatus) {
					field.setAccessible(true);
				}
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
				try {
					field.set(reflectObj, newValue);
				} finally {
					field.setAccessible(keepStatus);
					modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
				}
			} catch (Exception e) {
				Throwable t = e.getCause() != null && e instanceof InvocationTargetException ? e.getCause() : e;
				throw new RuntimeException(fieldName, t);
			}
		}

		/**
		 * If the underlying field is a static field, the reflectObj argument is ignored; it may be null.
		 *
		 * @param reflectObj may be null.
		 */
		static public void setField(Object reflectObj, Class<?> clsObj, String fieldName, Object newValue) {
			if (clsObj == null) {
				throw new IllegalArgumentException("(clsObj == null)");
			}
			if (fieldName == null || fieldName.length() == 0) {
				throw new IllegalArgumentException(
						"(fieldName == null || fieldName.length() == 0), fieldName=[" + fieldName + "]");
			}
			try {
				Field field = clsObj.getDeclaredField(fieldName);
				boolean keepStatus = field.isAccessible();
				if (!keepStatus) {
					field.setAccessible(true);
				}
				try {
					field.set(reflectObj, newValue);
				} finally {
					field.setAccessible(keepStatus);
				}
			} catch (Exception e) {
				Throwable t = e.getCause() != null && e instanceof InvocationTargetException ? e.getCause() : e;
				throw new RuntimeException(fieldName, t);
			}
		}

		/**
		 * @param reflectObj null for static field
		 */
		@SuppressWarnings("unchecked")
		public static <T> T getField(Object reflectObj, Class<?> clsObj, String fieldName) {
			Objects.requireNonNull(clsObj, "clsObj");
			Objects.requireNonNull(fieldName, "fieldName");
			try {
				Field field = clsObj.getDeclaredField(fieldName);
				boolean keepStatus = field.isAccessible();
				if (!keepStatus) {
					field.setAccessible(true);
				}
				try {
					return (T) field.get(reflectObj);
				} finally {
					field.setAccessible(keepStatus);
				}
			} catch (Exception e) {
				Throwable t = e.getCause() != null && e instanceof InvocationTargetException ? e.getCause() : e;
				throw new RuntimeException(fieldName, t);
			}
		}

		/**
		 * @param reflectObj null for static method
		 */
		public static <T> T invokeMethod(Object reflectObj, Class<?> clsObj, String methodName) {
			return invokeMethod(reflectObj, clsObj, methodName, new Class<?>[0], new Object[0]);
		}

		/**
		 * @param reflectObj null for static method
		 */
		@SuppressWarnings({"unchecked"})
		public static <T> T invokeMethod(Object reflectObj, Class<?> clsObj, String methodName, Class<?>[] argsTypes,
		                                 Object[] argsValues) {
			Objects.requireNonNull(clsObj, "clsObj");
			Objects.requireNonNull(methodName, "methodName");
			try {
				Method method = clsObj.getDeclaredMethod(methodName, argsTypes);
				boolean keepStatus = method.isAccessible();
				if (!keepStatus) {
					method.setAccessible(true);
				}
				try {
					return (T) method.invoke(reflectObj, argsValues);
				} finally {
					method.setAccessible(keepStatus);
				}
			} catch (Exception e) {
				Throwable t = e.getCause() != null && e instanceof InvocationTargetException ? e.getCause() : e;
				throw new RuntimeException(methodName, t);
			}
		}

		/**
		 * @param reflectObj may be null.
		 */
		static public <T> T invokeMethod(Object reflectObj, Method method, Object[] argsValues) {
			if (method == null) {
				throw new IllegalArgumentException("(method == null)");
			}
			if (method.getParameterTypes().length != argsValues.length) {
				throw new IllegalArgumentException(
						"(method.getParameterTypes().length != argsValues.length), method.parameterTypes.length="
								+ method.getParameterTypes().length + ", argsValues.length=" + argsValues.length);
			}
			try {
				boolean keepStatus = method.isAccessible();
				if (!keepStatus) {
					method.setAccessible(true);
				}
				try {
					@SuppressWarnings("unchecked")
					T obj = (T) method.invoke(reflectObj, argsValues);
					return obj;
				} finally {
					method.setAccessible(keepStatus);
				}
			} catch (Exception e) {
				Throwable t = e.getCause() != null && e instanceof InvocationTargetException ? e.getCause() : e;
				throw new RuntimeException(method.getName(), t);
			}
		}
	}

}

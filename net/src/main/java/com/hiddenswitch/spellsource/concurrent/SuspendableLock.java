package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.Semaphore;
import com.hazelcast.concurrent.semaphore.SemaphoreProxy;
import com.hazelcast.concurrent.semaphore.SemaphoreService;
import com.hazelcast.concurrent.semaphore.operations.AcquireOperation;
import com.hazelcast.concurrent.semaphore.operations.ReleaseOperation;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.impl.operationservice.InternalOperationService;
import com.hiddenswitch.spellsource.util.Hazelcast;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Lock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static io.vertx.ext.sync.Sync.awaitFiber;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * A suspendable, cluster-wide lock.
 * <p>
 * Internally, a Hazelcast semaphore with one permit represents the lock. Because many locks could be awaited at once,
 * Hazelcast async operations are used. This requires deconstructing the {@link SemaphoreProxy} and calling its
 * equivalent operations, except with async invocations.
 * <p>
 * We need to lock access to the hazelcast instance, because it might be reused by multiple verticles/vertx instances,
 * and its serializer's use of ThreadLocal is very poorly compatible with the kind of ThreadLocal bashing Quasar does
 * with its fibers implementation.
 */
public interface SuspendableLock {
	Semaphore LOCK_SERIALIZATION = new Semaphore(1);
	String LOCK_SEMAPHORE_PREFIX = "__vertx.";
	Logger LOGGER = LoggerFactory.getLogger(SuspendableLock.class);


	/**
	 * Obtains a lock that doesn't do anything.
	 *
	 * @return
	 */
	@NotNull
	static SuspendableLock noOpLock() {
		return new NoOpLock();
	}

	/**
	 * Obtains a lock using a Hazelcast cluster.
	 *
	 * @param name    The unique identifier of this lock
	 * @param timeout How long to wait for the lock. If the timeout is 0, the method waits indefinitely
	 * @return A lock that has been acquired.
	 * @throws VertxException with an inner cause of {@link TimeoutException} if the lock acquisition has timed out,
	 *                        otherwise the Hazelcast cluster experienced an error.
	 */
	@Suspendable
	@NotNull
	static SuspendableLock lock(@NotNull String name, long timeout) {
		return VertxLock.lock(name, timeout);
	}

	@Suspendable
	@NotNull
	static SuspendableLock lock(String name) {
		return VertxLock.lock(name);
	}

	static HazelcastInstanceImpl getHazelcastInstance(HazelcastInstance hazelcastInstance) {
		if (hazelcastInstance instanceof HazelcastInstanceProxy) {
			hazelcastInstance = ((HazelcastInstanceProxy) hazelcastInstance).getOriginal();
		}

		return (HazelcastInstanceImpl) hazelcastInstance;
	}

	@Suspendable
	void release();


	/**
	 * Destroys the lock, freeing up the resources it used in the cluster.
	 */
	@Suspendable
	void destroy();

	class VertxLock implements SuspendableLock {
		private final Lock lock;

		public VertxLock(Lock lock) {
			this.lock = lock;
		}

		@Suspendable
		@NotNull
		static VertxLock lock(String name, long timeout) {
			Lock lock = awaitResult(h -> Vertx.currentContext().owner().sharedData().getLockWithTimeout(name, timeout, h));
			return new VertxLock(lock);
		}

		@Suspendable
		@NotNull
		static VertxLock lock(String name) {
			Lock lock = awaitResult(h -> Vertx.currentContext().owner().sharedData().getLock(name, h));
			return new VertxLock(lock);
		}

		@Override
		@Suspendable
		public void release() {
			lock.release();
		}

		@Override
		@Suspendable
		public void destroy() {
		}
	}

	class HazelcastLock implements SuspendableLock {
		private final ISemaphore semaphore;

		private HazelcastLock(ISemaphore semaphore) {
			this.semaphore = semaphore;
		}

		@Override
		@Suspendable
		public void release() {

			/*
			Vertx.currentContext().executeBlocking(fut -> {
				try {
					semaphore.release();
				} finally {
					fut.complete();
				}
			}, false, null);

			// Releasing a semaphore instance will go fast, but below is the asynchronous implementation
*/

			HazelcastInstanceImpl hazelcastInstance = getHazelcastInstance(Hazelcast.getHazelcastInstance());
			GetPartitionAndService getPartitionAndService = new GetPartitionAndService(semaphore, hazelcastInstance).invoke();
			int partitionId = getPartitionAndService.getPartitionId();
			InternalOperationService operationService = getPartitionAndService.getOperationService();
			ReleaseOperation operation = new ReleaseOperation(semaphore.getName(), 1);
			operation.setPartitionId(partitionId);
			operation.setServiceName(SemaphoreService.SERVICE_NAME);

			operationService.asyncInvokeOnPartition(operation.getServiceName(),
					operation, operation.getPartitionId(), new ExecutionCallback<Boolean>() {
						@Override
						public void onResponse(Boolean response) {
							if (response != null && !response) {
								LOGGER.error("asyncInvokeOnPartition: Failed to unlock {}", semaphore.getName());
							}
						}

						@Override
						public void onFailure(Throwable t) {
							LOGGER.error("asyncInvokeOnPartition: ", t);
						}
					});
		}

		@Override
		@Suspendable
		public void destroy() {
			Vertx.currentContext().executeBlocking(fut -> {
				try {
					semaphore.destroy();
				} finally {
					fut.complete();
				}
			}, false, null);
		}

		/**
		 * Obtains a lock using a Hazelcast cluster.
		 *
		 * @param name    The unique identifier of this lock
		 * @param timeout How long to wait for the lock. If the timeout is 0, the method waits indefinitely
		 * @return A lock that has been acquired.
		 * @throws VertxException with an inner cause of {@link TimeoutException} if the lock acquisition has timed out,
		 *                        otherwise the Hazelcast cluster experienced an error.
		 */
		@Suspendable
		@NotNull
		static SuspendableLock lock(@NotNull String name, long timeout) {
			HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstance();

			// Lock accesses to getSemaphore
			Semaphore lock = LOCK_SERIALIZATION;
			ISemaphore iSemaphore;
			lock.acquireUninterruptibly();
			try {
				// This will typically happen pretty fast, so it's okay to use a blocking thread for this.
				iSemaphore = invoke(hazelcastInstance::getSemaphore, LOCK_SEMAPHORE_PREFIX + name);
			} finally {
				lock.release();
			}

			HazelcastInstanceImpl finalInstance = getHazelcastInstance(hazelcastInstance);

			return awaitFiber(fut1 -> {
				GetPartitionAndService getPartitionAndService = new GetPartitionAndService(iSemaphore, finalInstance).invoke();
				int partitionId = getPartitionAndService.getPartitionId();
				InternalOperationService operationService = getPartitionAndService.getOperationService();

				Operation acquireOperation = new AcquireOperation(LOCK_SEMAPHORE_PREFIX + name, 1, timeout)
						.setPartitionId(partitionId)
						.setServiceName(SemaphoreService.SERVICE_NAME);

				operationService.asyncInvokeOnPartition(acquireOperation.getServiceName(),
						acquireOperation, acquireOperation.getPartitionId(), new ExecutionCallback<Boolean>() {
							@Override
							public void onResponse(Boolean locked) {
								if (locked) {
									fut1.handle(Future.succeededFuture(new HazelcastLock(iSemaphore)));
								} else {
									fut1.handle(Future.failedFuture(new VertxException("timed out", new TimeoutException(name))));
								}
							}

							@Override
							public void onFailure(Throwable t) {
								fut1.handle(Future.failedFuture(new VertxException(name, t)));
							}
						});
			});

		}

		@Suspendable
		static SuspendableLock lock(String name) {
			return lock(name, -1);
		}
	}

	class NoOpLock implements SuspendableLock {

		@Override
		public void release() {
		}

		@Override
		public void destroy() {

		}
	}

	/**
	 * Represents an internal information-gathering mechanism to let us do the work that {@link SemaphoreProxy} does when
	 * it acquires and releases locks. We need this information in order to do so using async primitives.
	 */
	class GetPartitionAndService {
		private ISemaphore iSemaphore;
		private HazelcastInstanceImpl finalInstance;
		private int partitionId;
		private InternalOperationService operationService;

		public GetPartitionAndService(ISemaphore iSemaphore, HazelcastInstanceImpl finalInstance) {
			this.iSemaphore = iSemaphore;
			this.finalInstance = finalInstance;
		}

		public int getPartitionId() {
			return partitionId;
		}

		public InternalOperationService getOperationService() {
			return operationService;
		}

		public GetPartitionAndService invoke() {
			partitionId = 0;

			try {
				Field partitionIdField = SemaphoreProxy.class.getDeclaredField("partitionId");
				partitionIdField.setAccessible(true);
				partitionId = partitionIdField.getInt(iSemaphore);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				throw new VertxException(e);
			}

			operationService = finalInstance.node.getNodeEngine().getOperationService();
			return this;
		}
	}
}

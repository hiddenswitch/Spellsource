package com.hiddenswitch.spellsource.concurrent;

import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.concurrent.ReentrantLock;
import com.hazelcast.concurrent.semaphore.SemaphoreProxy;
import com.hazelcast.concurrent.semaphore.SemaphoreService;
import com.hazelcast.concurrent.semaphore.operations.AcquireOperation;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static io.vertx.ext.sync.Sync.awaitFiber;

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
	ReentrantLock LOCK_SERIALIZATION = new ReentrantLock();
	String LOCK_SEMAPHORE_PREFIX = "__vertx.";

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
		ReentrantLock lock = LOCK_SERIALIZATION;
		ISemaphore iSemaphore;
		lock.lock();
		try {
			// This will typically happen pretty fast, so it's okay to use a blocking thread for this.
			iSemaphore = invoke(hazelcastInstance::getSemaphore, LOCK_SEMAPHORE_PREFIX + name);
		} finally {
			lock.unlock();
		}

		HazelcastInstanceImpl finalInstance = getHazelcastInstance(hazelcastInstance);

		return awaitFiber(fut1 -> {
			GetPartitionAndService getPartitionAndService = new GetPartitionAndService(iSemaphore, finalInstance).invoke();
			int partitionId = getPartitionAndService.getPartitionId();
			InternalOperationService operationService = getPartitionAndService.getOperationService();

			Operation acquireOperation = new AcquireOperation(LOCK_SEMAPHORE_PREFIX + name, 1, timeout)
					.setPartitionId(partitionId)
					.setServiceName(SemaphoreService.SERVICE_NAME);


			ReentrantLock lockA = LOCK_SERIALIZATION;
			lockA.lock();
			try {
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
			} finally {
				lockA.unlock();
			}
		});

	}

	static HazelcastInstanceImpl getHazelcastInstance(HazelcastInstance hazelcastInstance) {
		if (hazelcastInstance instanceof HazelcastInstanceProxy) {
			hazelcastInstance = ((HazelcastInstanceProxy) hazelcastInstance).getOriginal();
		}

		return (HazelcastInstanceImpl) hazelcastInstance;
	}

	@Suspendable
	void release();

	@Suspendable
	static SuspendableLock lock(String name) {
		return lock(name, -1);
	}

	/**
	 * Destroys the lock, freeing up the resources it used in the cluster.
	 */
	@Suspendable
	void destroy();

	class HazelcastLock implements SuspendableLock {
		private final ISemaphore semaphore;

		private HazelcastLock(ISemaphore semaphore) {
			this.semaphore = semaphore;
		}

		@Override
		@Suspendable
		public void release() {

			Vertx.currentContext().executeBlocking(fut -> {
				try {
					semaphore.release();
				} finally {
					fut.complete();
				}
			}, false, null);

			// Releasing a semaphore instance will go fast, but below is the asynchronous implementation

			/*
			HazelcastInstanceImpl hazelcastInstance = getHazelcastInstance(Hazelcast.getHazelcastInstance());
			GetPartitionAndService getPartitionAndService = new GetPartitionAndService(semaphore, hazelcastInstance).invoke();
			int partitionId = getPartitionAndService.getPartitionId();
			InternalOperationService operationService = getPartitionAndService.getOperationService();
			ComparisonOperation operation = new ReleaseOperation(semaphore.getName(), 1)
					.setPartitionId(partitionId)
					.setServiceName(SemaphoreService.SERVICE_NAME);

			ReentrantLock lock = LOCK_SERIALIZATION.computeIfAbsent(hazelcastInstance, (ignored) -> new ReentrantLock());
			lock.lock();
			try {
				operationService.asyncInvokeOnPartition(operation.getServiceName(),
						operation, operation.getPartitionId(), new ExecutionCallback<Boolean>() {
							@Override
							public void onResponse(Boolean response) {
							}

							@Override
							public void onFailure(Throwable t) {
							}
						});
			} finally {
				lock.unlock();
			}
			*/
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

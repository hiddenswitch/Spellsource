package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.hazelcast.concurrent.semaphore.SemaphoreProxy;
import com.hazelcast.concurrent.semaphore.SemaphoreService;
import com.hazelcast.concurrent.semaphore.operations.AcquireOperation;
import com.hazelcast.concurrent.semaphore.operations.ReleaseOperation;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.impl.operationservice.InternalOperationService;
import com.hiddenswitch.spellsource.util.Hazelcast;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static io.vertx.ext.sync.Sync.awaitFiber;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * A suspendable, cluster-wide lock.
 * <p>
 * Internally, a Hazelcast semaphore with one permit represents the lock. Because many locks could be awaited at once,
 * Hazelcast async operations are used. This requires deconstructing the {@link SemaphoreProxy} and calling its
 * equivalent operations, except with async invocations.
 * <p>
 * In the condition where the Hazelcast serializer is used on a Fiber-hosting thread, the async
 */
public interface SuspendableLock {
	ReentrantLock LOCK_SERIALIZATION =new ReentrantLock();
	String LOCK_SEMAPHORE_PREFIX = "__vertx.";

	@Suspendable
	static SuspendableLock lock(String name, long timeout) {
		HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstance();

		// Lock accesses to getSemaphore
//		ISemaphore semaphore = null;
		ISemaphore iSemaphore;
		ReentrantLock lock = LOCK_SERIALIZATION; //.computeIfAbsent(hazelcastInstance, (ignored) -> new ReentrantLock());
		lock.lock();
		try {
//			while (semaphore == null) {
//				try {
					iSemaphore = hazelcastInstance.getSemaphore(LOCK_SEMAPHORE_PREFIX + name);
//				} catch (HazelcastInstanceNotActiveException syncIssue) {
//					try {
//						Strand.yield();
//					} catch (SuspendExecution ignored) {
//					}
//				}
//			}
		} finally {
			lock.unlock();
		}

//		ISemaphore iSemaphore = semaphore;


		HazelcastInstanceImpl finalInstance = getHazelcastInstance(hazelcastInstance);

		return awaitFiber(fut1 -> {
			GetPartitionAndService getPartitionAndService = new GetPartitionAndService(iSemaphore, finalInstance).invoke();
			int partitionId = getPartitionAndService.getPartitionId();
			InternalOperationService operationService = getPartitionAndService.getOperationService();

			Operation acquireOperation = new AcquireOperation(LOCK_SEMAPHORE_PREFIX + name, 1, timeout)
					.setPartitionId(partitionId)
					.setServiceName(SemaphoreService.SERVICE_NAME);


			ReentrantLock lockA = LOCK_SERIALIZATION; //.computeIfAbsent(hazelcastInstance, (ignored) -> new ReentrantLock());
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

	class HazelcastLock implements SuspendableLock {
		private final ISemaphore semaphore;

		private HazelcastLock(ISemaphore semaphore) {
			this.semaphore = semaphore;
		}

		@Override
		@Suspendable
		public void release() {

			Vertx.currentContext().executeBlocking(fut -> {
				semaphore.release();
				fut.complete();
			}, false, null);


			/*
			HazelcastInstanceImpl hazelcastInstance = getHazelcastInstance(Hazelcast.getHazelcastInstance());
			GetPartitionAndService getPartitionAndService = new GetPartitionAndService(semaphore, hazelcastInstance).invoke();
			int partitionId = getPartitionAndService.getPartitionId();
			InternalOperationService operationService = getPartitionAndService.getOperationService();
			Operation operation = new ReleaseOperation(semaphore.getName(), 1)
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
	}

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

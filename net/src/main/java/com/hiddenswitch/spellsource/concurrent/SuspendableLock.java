package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.concurrent.semaphore.SemaphoreProxy;
import com.hazelcast.concurrent.semaphore.SemaphoreService;
import com.hazelcast.concurrent.semaphore.operations.AcquireOperation;
import com.hazelcast.concurrent.semaphore.operations.InitOperation;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.spi.InternalCompletableFuture;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.impl.operationservice.InternalOperationService;
import com.hiddenswitch.spellsource.util.Hazelcast;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Lock;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static com.hiddenswitch.spellsource.util.Sync.invoke0;
import static com.hiddenswitch.spellsource.util.Sync.invoke1;
import static io.vertx.ext.sync.Sync.awaitEvent;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public interface SuspendableLock {
	String LOCK_SEMAPHORE_PREFIX = "__vertx.";

	@Suspendable
	static SuspendableLock lock(String name, long timeout) {
		// Hand roll a
		return awaitResult(fut1 -> {
			HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstance();
			ISemaphore iSemaphore = hazelcastInstance.getSemaphore(LOCK_SEMAPHORE_PREFIX + name);

			int partitionId = 0;

			try {
				Field partitionIdField = SemaphoreProxy.class.getDeclaredField("partitionId");
				partitionIdField.setAccessible(true);
				partitionId = partitionIdField.getInt(iSemaphore);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				fut1.handle(Future.failedFuture(new VertxException(e)));
				return;
			}

			if (hazelcastInstance instanceof HazelcastInstanceProxy) {
				hazelcastInstance = ((HazelcastInstanceProxy) hazelcastInstance).getOriginal();
			}

			InternalOperationService operationService = ((HazelcastInstanceImpl) hazelcastInstance).node.nodeEngine.getOperationService();

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
			invoke0(semaphore::release);
		}
	}
}

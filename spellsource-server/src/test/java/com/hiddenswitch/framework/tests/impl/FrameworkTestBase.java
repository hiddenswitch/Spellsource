package com.hiddenswitch.framework.tests.impl;

import com.hiddenswitch.framework.Gateway;
import com.hiddenswitch.framework.tests.applications.StandaloneApplication;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.impl.future.FutureInternal;
import io.vertx.core.impl.future.Listener;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.vertx.await.Async.await;
import static io.vertx.core.CompositeFuture.all;
import static org.testcontainers.Testcontainers.exposeHostPorts;

@ExtendWith({VertxExtension.class})
@Testcontainers
public class FrameworkTestBase {
	protected static ToxiproxyContainer TOXIPROXY = new ToxiproxyContainer(DockerImageName.parse("ghcr.io/shopify/toxiproxy:latest").asCompatibleSubstituteFor("shopify/toxiproxy"));
	private static ToxiproxyContainer.ContainerProxy toxicGrpcProxy;

	public static ToxiproxyContainer.ContainerProxy toxicGrpcProxy() {
		return toxicGrpcProxy;
	}

	@BeforeAll
	protected static void startContainers() throws InterruptedException {
		if (!StandaloneApplication.defaultConfigurationAndServices()) {
			return;
		}

		exposeHostPorts(Gateway.defaultGrpcPort());

		Startables.deepStart(Stream.of(TOXIPROXY)).join();
		toxicGrpcProxy = TOXIPROXY.getProxy("host.testcontainers.internal", Gateway.defaultGrpcPort());
	}

	@AfterEach
	public void delay() throws InterruptedException {
		TimeUnit.SECONDS.sleep(3);
	}

	protected Future<String> startGateway(Vertx vertx) {
		return startGateway(vertx, Gateway.defaultGrpcPort());
	}

	protected Future<String> startGateway(Vertx vertx, int port) {
		return vertx.deployVerticle(() -> new Gateway(port), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(CpuCoreSensor.availableProcessors()));
	}

	public void testVirtual(Vertx vertx, VertxTestContext vertxTestContext, VertxTestContext.ExecutionBlock runnable) {
		var verticle = new AbstractVirtualThreadVerticle() {
			@Override
			public void startVirtual() throws Exception {
				vertxTestContext.verify(runnable);
			}
		};
		vertx.deployVerticle(verticle, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
				.onComplete(vertxTestContext.succeedingThenComplete())
				.onSuccess(vertx::undeploy);
	}

	public static class Checkpoint implements FutureInternal<Void> {
		private final Promise<Void> finished = Promise.promise();
		private final Future<Void> future = finished.future();
		private int times;

		private Checkpoint(int times) {
			this.times = times;
		}

		public static Checkpoint checkpoint(int times) {
			return new Checkpoint(times);
		}

		public static Future<Void> awaitCheckpoints(Checkpoint... checkpoints) {
			return Future.all(Arrays.asList(checkpoints)).map((Void) null);
		}

		@Override
		public boolean isComplete() {
			return future.isComplete();
		}

		@Override
		public Future<Void> onComplete(Handler<AsyncResult<Void>> handler) {
			return future.onComplete(handler);
		}

		@Override
		public Future<Void> onSuccess(Handler<Void> handler) {
			return future.onSuccess(handler);
		}

		@Override
		public Future<Void> onFailure(Handler<Throwable> handler) {
			return future.onFailure(handler);
		}

		@Override
		public Void result() {
			return future.result();
		}

		@Override
		public Throwable cause() {
			return future.cause();
		}

		@Override
		public boolean succeeded() {
			return future.succeeded();
		}

		@Override
		public boolean failed() {
			return future.failed();
		}

		@Override
		public <U> Future<U> flatMap(Function<Void, Future<U>> mapper) {
			return future.flatMap(mapper);
		}

		@Override
		public <U> Future<U> compose(Function<Void, Future<U>> mapper) {
			return future.compose(mapper);
		}

		@Override
		public Future<Void> recover(Function<Throwable, Future<Void>> mapper) {
			return future.recover(mapper);
		}

		@Override
		public <U> Future<U> compose(Function<Void, Future<U>> successMapper, Function<Throwable, Future<U>> failureMapper) {
			return future.compose(successMapper, failureMapper);
		}

		@Override
		public <U> Future<U> transform(Function<AsyncResult<Void>, Future<U>> mapper) {
			return future.transform(mapper);
		}

		@Override
		public <U> Future<Void> eventually(Function<Void, Future<U>> mapper) {
			return future.eventually(mapper);
		}

		@Override
		public <U> Future<U> map(Function<Void, U> mapper) {
			return future.map(mapper);
		}

		@Override
		public <V> Future<V> map(V value) {
			return future.map(value);
		}

		@Override
		public <V> Future<V> mapEmpty() {
			return future.mapEmpty();
		}

		@Override
		public Future<Void> otherwise(Function<Throwable, Void> mapper) {
			return future.otherwise(mapper);
		}

		@Override
		public Future<Void> otherwise(Void value) {
			return future.otherwise(value);
		}

		@Override
		public Future<Void> otherwiseEmpty() {
			return future.otherwiseEmpty();
		}

		@Override
		public Future<Void> expecting(Expectation<? super Void> expectation) {
			return future.expecting(expectation);
		}

		@Override
		public Future<Void> timeout(long delay, TimeUnit unit) {
			return future.timeout(delay, unit);
		}

		@Override
		public CompletionStage<Void> toCompletionStage() {
			return future.toCompletionStage();
		}

		public Future<Void> flag() {
			this.times -= 1;
			if (this.times < 0) {
				return Future.failedFuture("flagged too many times");
			}

			if (this.times == 0) {
				finished.tryComplete();
			}
			return Future.succeededFuture();
		}

		@Override
		public ContextInternal context() {
			return ((FutureInternal<Void>) future).context();
		}

		@Override
		public void addListener(Listener<Void> listener) {
			((FutureInternal<Void>) future).addListener(listener);
		}

		@Override
		public void removeListener(Listener<Void> listener) {
			((FutureInternal<Void>) future).removeListener(listener);
		}
	}
}

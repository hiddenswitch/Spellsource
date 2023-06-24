package com.hiddenswitch.framework;

import com.be_hase.grpc.micrometer.MicrometerServerInterceptor;
import com.netflix.concurrency.limits.grpc.server.ConcurrencyLimitServerInterceptor;
import com.netflix.concurrency.limits.grpc.server.GrpcServerLimiterBuilder;
import com.netflix.concurrency.limits.limit.Gradient2Limit;
import com.netflix.concurrency.limits.limit.WindowedLimit;
import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.micrometer.core.instrument.Metrics;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Gateway extends AbstractVerticle {
	private static Logger LOGGER = LoggerFactory.getLogger(Gateway.class);
	private final int port;
	List<Closeable> closeables = new ArrayList<>();
	private boolean useVertxNativeGrpcServer = false;
	private Matchmaking.Services matchmaking;

	public Gateway() {
		this(Environment.getConfiguration().getGrpcConfiguration().getPort());
	}

	public Gateway(int port) {
		this.port = port;
	}

	public boolean useVertxNativeGrpcServer() {
		return useVertxNativeGrpcServer;
	}

	public Gateway setUseVertxNativeGrpcServer(boolean useVertxNativeGrpcServer) {
		this.useVertxNativeGrpcServer = useVertxNativeGrpcServer;
		return this;
	}

	public static int defaultGrpcPort() {
		return Environment.getConfiguration().getGrpcConfiguration().getPort();
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		var configuration = Environment.getConfiguration();

		this.matchmaking = Matchmaking.services();
		CompositeFuture.all(
						Legacy.services(),
						Legacy.unauthenticatedCards(),
						matchmaking.serviceDefinitionFuture(),
						Accounts.unauthenticatedService(),
						Accounts.authenticatedService(),
						Games.services())
				.compose(services -> {
					var server = useVertxNativeGrpcServer ? new VertxNativeGrpcServer(vertx, port) : new NettyGrpcServer(vertx, port);
					var inited = server.init();


					var list = services.list();
					for (var service : list) {
						var boundService = service instanceof BindableService ? ((BindableService) service).bindService() : (ServerServiceDefinition) service;
						// basic interceptors for grpc
						var interceptors = new ArrayList<ServerInterceptor>();
						if (configuration.hasMetrics()) {
							interceptors.add(new MicrometerServerInterceptor(Metrics.globalRegistry));
						}
						if (configuration.hasJaeger() && configuration.getJaeger().getEnabled()) {
							interceptors.add(TracingServerInterceptor
									.newBuilder()
									.withTracer(GlobalTracer.get())
									.withStreaming()
									.withVerbosity()
									.build());
						}
						if (configuration.hasRateLimiter() && configuration.getRateLimiter().getEnabled()) {
							interceptors.add(ConcurrencyLimitServerInterceptor.newBuilder(
									new GrpcServerLimiterBuilder()
											.partitionResolver(context -> {
												var address = context.getCall().getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
												if (address instanceof InetSocketAddress) {
													var inetSocketAddress = (InetSocketAddress) address;
													var hostAddress = inetSocketAddress.getAddress().getHostAddress();
													if (hostAddress.equals("127.0.0.1")) {
														hostAddress += ":" + inetSocketAddress.getPort();
													}
													return hostAddress;
												}

												return address.toString();
											})
											.limit(WindowedLimit.newBuilder()
													.build(Gradient2Limit.newBuilder()
															.build()))
											.build()).build());
						}
						boundService = ServerInterceptors.intercept(boundService, interceptors);
						server.bind(boundService);
					}

					server.bind(ProtoReflectionService.newInstance().bindService());
					closeables.add(matchmaking.clientMatchmakingService());
					closeables.add(server);
					return inited.compose(v -> server.start());
				})
				.onFailure(Environment.onFailure())
				.onComplete(startPromise);
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		CompositeFuture.all(closeables.stream().map(closeable -> {
					var promise = Promise.<Void>promise();
					closeable.close(promise);
					return promise.future();
				}).collect(Collectors.toList()))
				.map((Void) null)
				.onComplete(stopPromise);
	}

	interface GatewayGrpcServer extends Closeable {
		void bind(ServerServiceDefinition serverServiceDefinition);

		Future<Void> init();

		Future<Void> start();
	}

	static class NettyGrpcServer implements GatewayGrpcServer {

		private final Vertx vertx;
		private final int port;
		private VertxServerBuilder builder;
		private VertxServer server;

		public NettyGrpcServer(Vertx vertx, int port) {
			this.vertx = vertx;
			this.port = port;
		}

		@Override
		public void bind(ServerServiceDefinition serverServiceDefinition) {
			builder.addService(serverServiceDefinition);
		}

		@Override
		public Future<Void> init() {
			this.builder = VertxServerBuilder.forPort(vertx, port);
			return Future.succeededFuture();
		}

		@Override
		public Future<Void> start() {
			var nettyServerBuilder = builder.nettyBuilder();
			var configuration = Environment.getConfiguration();
			nettyServerBuilder
//					.withOption(ChannelOption.ALLOCATOR, VertxByteBufAllocator.UNPOOLED_ALLOCATOR)
					.maxConnectionIdle(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
					.maxConnectionAge(29, TimeUnit.DAYS)
					.keepAliveTime(configuration.getGrpcConfiguration().getServerKeepAliveTimeMillis(), TimeUnit.MILLISECONDS)
					.keepAliveTimeout(configuration.getGrpcConfiguration().getServerKeepAliveTimeoutMillis(), TimeUnit.MILLISECONDS)
					.permitKeepAliveTime(Math.max(configuration.getGrpcConfiguration().getServerKeepAliveTimeMillis() - 400, 100), TimeUnit.MILLISECONDS)
					.permitKeepAliveWithoutCalls(configuration.getGrpcConfiguration().getServerPermitKeepAliveWithoutCalls());
			this.server = builder.build();
			var promise = Promise.<Void>promise();
			this.server.start(promise);
			return promise.future();
		}

		@Override
		public void close(Promise<Void> completion) {
			this.server.shutdown(completion);
		}
	}

	static class VertxNativeGrpcServer implements GatewayGrpcServer {

		private Vertx vertx;
		private int port;
		private HttpServer httpServer;
		private GrpcServer server;

		public VertxNativeGrpcServer(Vertx vertx, int port) {
			this.vertx = vertx;
			this.port = port;
		}

		@Override
		public void bind(ServerServiceDefinition serverServiceDefinition) {
			GrpcServiceBridge.bridge(serverServiceDefinition).bind(server);
		}

		@Override
		public void close(Promise<Void> closePromise) {
			this.httpServer.close(closePromise);
		}

		@Override
		public Future<Void> init() {
			this.server = GrpcServer.server(vertx);
			this.httpServer = vertx.createHttpServer((HttpServerOptions) new HttpServerOptions()
					.setReadIdleTimeout(Integer.MAX_VALUE)
					.setWriteIdleTimeout(Integer.MAX_VALUE)
					.setIdleTimeout(Integer.MAX_VALUE)
					.setTcpUserTimeout(Integer.MAX_VALUE));
			return httpServer.requestHandler(server)
					.listen(port)
					.map((Void) null);
		}

		@Override
		public Future<Void> start() {
			return Future.succeededFuture();
		}
	}
}

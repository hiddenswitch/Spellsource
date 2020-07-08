package io.vertx.test.core;

import io.etcd.jetcd.launcher.junit4.EtcdClusterResource;
import io.vertx.core.shareddata.ClusteredAsyncMapTest;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.etcd.EtcdClusterManager;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;

import java.util.concurrent.atomic.AtomicInteger;


public class EtcdClusteredAsyncMapTest extends ClusteredAsyncMapTest {

	private AtomicInteger prefix = new AtomicInteger();

	@ClassRule
	public static final EtcdClusterResource cluster = new EtcdClusterResource("test-etcd", 3, false, false);

	@Before
	@Override
	public void before() throws Exception {
		prefix.incrementAndGet();
		super.before();
	}

	@Override
	protected ClusterManager getClusterManager() {
		return new EtcdClusterManager(cluster.getClientEndpoints().get(0).getHost(), cluster.getClientEndpoints().get(0).getPort(), "vertx-test-" + prefix.get());
	}
}

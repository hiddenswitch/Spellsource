package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ZookeeperContainer extends GenericContainer<ZookeeperContainer> {
	public ZookeeperContainer() {
		super("zookeeper:3.6.1");
		withExposedPorts(2181, 2888, 3888, 8080);
		waitingFor(Wait.forLogMessage(".*Snapshotting: 0x0.*", 2));
	}

	public String getZookeeperUrl() {
		return getHost() + ":" + getMappedPort(2181);
	}
}

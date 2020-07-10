package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;

public class RedisContainer extends GenericContainer<RedisContainer> {

	private static final int REDIS_PORT = 6379;

	public RedisContainer() {
		super("redis:6.0.5");
		withExposedPorts(REDIS_PORT);
		waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));
	}

	public void clear() throws IOException, InterruptedException {
		execInContainer("redis-cli", "FLUSHALL");
	}

	public String getRedisUrl() {
		return "redis://" + getHost() + ":" + getMappedPort(6379);
	}
}

package com.hiddenswitch.containers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;

public class RedisContainer extends GenericContainer<RedisContainer> {

	private static final int REDIS_PORT = 6379;

	public RedisContainer() {
		super("redis:6.0");
		withExposedPorts(REDIS_PORT);
		waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));
	}

	public void clear() throws IOException, InterruptedException {
		execInContainer("redis-cli", "FLUSHALL");
	}

	@Override
	protected void containerIsStarted(InspectContainerResponse containerInfo) {
		super.containerIsStarted(containerInfo);
		try {
			ExecResult res = execInContainer("redis-cli", "CONFIG", "SET", "notify-keyspace-events", "Exg");
			if (!res.getStdout().contains("OK")) {
				throw new AssertionError(res.getStdout());
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String getRedisUrl() {
		return "redis://" + getHost() + ":" + getMappedPort(6379);
	}
}

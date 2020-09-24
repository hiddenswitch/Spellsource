package com.hiddenswitch.containers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class RealtimeContainer extends GenericContainer<RealtimeContainer> {
	private static Logger LOGGER = LoggerFactory.getLogger(RealtimeContainer.class);
	private final int realtimePort;

	public RealtimeContainer(String secretKeyBase, int realtimePort) {
		super("supabase/realtime:latest");
		this.realtimePort = realtimePort;
		withExposedPorts(realtimePort);
		withEnv("PORT", Integer.toString(realtimePort));
		withEnv("SECRET_KEY_BASE", secretKeyBase);
		waitingFor(Wait.forLogMessage(".*Access RealtimeWeb.Endpoint at.*", 1));
	}

	@Override
	protected void containerIsStarted(InspectContainerResponse containerInfo) {
		super.containerIsStarted(containerInfo);
		Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
		followOutput(logConsumer);
	}

	public String getRealtimeUrl() {
		return "ws://" + getHost() + ":" + getMappedPort(realtimePort) + "/socket";
	}
}
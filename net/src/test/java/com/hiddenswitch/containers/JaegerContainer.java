package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.Wait;

public class JaegerContainer extends GenericContainer<JaegerContainer> {

	private final int agentPort;

	public JaegerContainer() {
		this("jaegertracing/all-in-one:latest");
	}

	public JaegerContainer(String dockerImageName) {
		super(dockerImageName);
		agentPort = 6831;
		withEnv("COLLECTOR_ZIPKIN_HTTP_PORT", "9411");
		withExposedPorts(5778, 16686, 14268, 14250, 9411);
		addFixedExposedPort(agentPort, agentPort, InternetProtocol.UDP);
		waitingFor(Wait.forLogMessage(".*\"status\":\"ready\".*", 1));
	}

	public String getAgentHost() {
		return getHost();
	}

	public Integer getAgentPort() {
		return agentPort;
	}

	public String getFrontendUrl() {
		return "http://" + getHost() + ":" + getMappedPort(16686);
	}
}

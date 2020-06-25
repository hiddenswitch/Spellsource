package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

public class GrafanaContainer extends GenericContainer<GrafanaContainer> {
	public static final String GRAFANA_STORAGE_DIR = "/var/lib/grafana";
	public GrafanaContainer(String jaegerAgentHost, int jaegerAgentPort) {
		super(new ImageFromDockerfile("spellsource/grafana", false)
				.withFileFromPath(".", Path.of(System.getProperty("rootProject.projectDir", "./"), "grafana")));
		withExposedPorts(3000);
		withEnv("GF_SECURITY_ADMIN_USER", "admin");
		withEnv("GF_SECURITY_ADMIN_PASSWORD", "admin");
		withEnv("JAEGER_AGENT_HOST", jaegerAgentHost);
		withEnv("JAEGER_AGENT_PORT", Integer.toString(jaegerAgentPort));
		waitingFor(Wait.forHttp("/").forPort(3000));
	}

	public String getGrafanaUrl() {
		return "http://" + getHost() + ":" + getMappedPort(3000);
	}
}

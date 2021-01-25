package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PrometheusContainer extends GenericContainer<PrometheusContainer> {
	public PrometheusContainer() {
		super(new ImageFromDockerfile("spellsource/prometheus", false)
				.withFileFromPath(".", Paths.get(System.getProperty("rootProject.projectDir", "./"), "prometheus")));
		withExposedPorts(9090);
		waitingFor(Wait.forListeningPort());
	}

	public String getPrometheusUrl() {
		return "http://" + getHost() + ":" + getMappedPort(9090);
	}
}

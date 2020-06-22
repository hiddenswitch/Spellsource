package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

public class KeycloakContainer extends GenericContainer<KeycloakContainer> {

	private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak";
	private static final String KEYCLOAK_VERSION = "10.0.2";

	private static final int KEYCLOAK_PORT_HTTP = 8080;

	private static final String KEYCLOAK_ADMIN_USER = "admin";
	private static final String KEYCLOAK_ADMIN_PASSWORD = "admin";
	private static final String KEYCLOAK_AUTH_PATH = "/auth";

	private String adminUsername = KEYCLOAK_ADMIN_USER;
	private String adminPassword = KEYCLOAK_ADMIN_PASSWORD;

	private String importFile;

	public KeycloakContainer() {
		this(KEYCLOAK_IMAGE + ":" + KEYCLOAK_VERSION);
	}

	/**
	 * Create a KeycloakContainer by passing the full docker image name
	 *
	 * @param dockerImageName Full docker image name, e.g. quay.io/keycloak/keycloak:8.0.1
	 */
	public KeycloakContainer(String dockerImageName) {
		super(dockerImageName);
		withExposedPorts(KEYCLOAK_PORT_HTTP);
		setWaitStrategy(Wait
				.forHttp(KEYCLOAK_AUTH_PATH)
				.forPort(KEYCLOAK_PORT_HTTP)
				.withStartupTimeout(Duration.ofMinutes(2))
		);
	}

	@Override
	protected void configure() {
		withCommand(
				"-c standalone.xml", // don't start infinispan cluster
				"-Dkeycloak.profile.feature.upload_scripts=enabled" // enable script uploads
		);

		withEnv("KEYCLOAK_USER", adminUsername);
		withEnv("KEYCLOAK_PASSWORD", adminPassword);

		if (importFile != null) {
			var importFileInContainer = "/tmp/" + importFile;
			withCopyFileToContainer(MountableFile.forClasspathResource(importFile), importFileInContainer);
			withEnv("KEYCLOAK_IMPORT", importFileInContainer);
		}
	}

	public KeycloakContainer withRealmImportFile(String importFile) {
		this.importFile = importFile;
		return self();
	}

	public KeycloakContainer withAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
		return self();
	}

	public KeycloakContainer withAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		return self();
	}

	public String getAuthServerUrl() {
		return String.format("http://%s:%s%s", getContainerIpAddress(), getMappedPort(KEYCLOAK_PORT_HTTP), KEYCLOAK_AUTH_PATH);
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public int getHttpPort() {
		return getMappedPort(KEYCLOAK_PORT_HTTP);
	}

	protected String getKeycloakVersion() {
		return KEYCLOAK_VERSION;
	}

	private boolean isNotBlank(String s) {
		return s != null && !s.trim().isEmpty();
	}

}
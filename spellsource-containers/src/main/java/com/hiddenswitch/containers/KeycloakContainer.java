package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

public class KeycloakContainer extends GenericContainer<KeycloakContainer> {
	private static final int KEYCLOAK_PORT_HTTP = 8080;

	private static final String KEYCLOAK_ADMIN_USER = "admin";
	private static final String KEYCLOAK_ADMIN_PASSWORD = "admin";
	private static final String KEYCLOAK_AUTH_PATH = "/auth";

	private String adminUsername = KEYCLOAK_ADMIN_USER;
	private String adminPassword = KEYCLOAK_ADMIN_PASSWORD;

	private String importFile;
	private String tlsCertFilename;
	private String tlsKeyFilename;
	private boolean useTls = false;

	/**
	 * Create a KeycloakContainer by passing the full docker image name
	 */
	public KeycloakContainer() {
		super("doctorpangloss/keycloak-standalone:latest");
		withExposedPorts(KEYCLOAK_PORT_HTTP);
		withReuse(false);
		setWaitStrategy(Wait
				.forHttp(KEYCLOAK_AUTH_PATH)
				.forPort(KEYCLOAK_PORT_HTTP)
				.withStartupTimeout(Duration.ofSeconds(60))
		);
	}

	@Override
	protected void configure() {

		withEnv("KEYCLOAK_ADMIN_USER", adminUsername);
		withEnv("KEYCLOAK_ADMIN_PASSWORD", adminPassword);
		withEnv("KEYCLOAK_DATABASE_SCHEMA", "keycloak");

		if (importFile != null) {
			String importFileInContainer = "/tmp/" + importFile;
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

	public KeycloakContainer useTls() {
		// tls.crt and tls.key are provided with this testcontainer
		return useTls("tls.crt", "tls.key");
	}

	public KeycloakContainer useTls(String tlsCertFilename, String tlsKeyFilename) {
		this.tlsCertFilename = tlsCertFilename;
		this.tlsKeyFilename = tlsKeyFilename;
		this.useTls = true;
		return self();
	}

	public String getAuthServerUrl() {
		return String.format("http%s://%s:%s%s", "", getContainerIpAddress(), getMappedPort(KEYCLOAK_PORT_HTTP), KEYCLOAK_AUTH_PATH);
	}

	public KeycloakContainer withPostgres(String postgresHostPort, String databaseName, String username, String password) {
		withEnv("KEYCLOAK_DATABASE_HOST", postgresHostPort);
		withEnv("KEYCLOAK_DATABASE_USER", username);
		withEnv("KEYCLOAK_DATABASE_PASSWORD", password);
		withEnv("KEYCLOAK_DATABASE_NAME", databaseName);
		return self();
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

	private boolean isNotBlank(String s) {
		return s != null && !s.trim().isEmpty();
	}

}

package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;

public class PostgresContainer extends GenericContainer<PostgresContainer> {

	public static final int POSTGRESQL_PORT = 5432;

	public PostgresContainer(String username, String password, String databaseName) {
		super("docker.io/postgres:18.6-trixie@sha256:edb0c08dc33befd0262ab3b5cf1c4f3f8cfd8b09e88c05b45adee29d6761ddb6");
		withExposedPorts(POSTGRESQL_PORT);
		withEnv("POSTGRES_DB", databaseName);
		withEnv("POSTGRES_USER", username);
		withEnv("POSTGRES_PASSWORD", password);
		withEnv("POSTGRES_LOG_STATEMENT", "all");
		withEnv("POSTGRES_MAX_CONNECTIONS", "24000");
		withCommand("/bin/bash", "-c", "mkdir -pv /docker-entrypoint-initdb.d/ && echo 'create schema if not exists keycloak;' > /docker-entrypoint-initdb.d/init.sql && docker-entrypoint.sh postgres -c max_connections=24000");
	}

	public String getHostAndPort() {
		return getHost() + ":" + getMappedPort(PostgresContainer.POSTGRESQL_PORT);
	}
}

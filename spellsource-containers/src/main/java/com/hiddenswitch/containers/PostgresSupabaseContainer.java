package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;

public class PostgresSupabaseContainer extends GenericContainer<PostgresSupabaseContainer> {

	public static final int POSTGRESQL_PORT = 5432;

	public PostgresSupabaseContainer(String username, String password, String databaseName) {
		super("docker.io/postgres:13.11-bullseye");
		withExposedPorts(POSTGRESQL_PORT);
		withEnv("POSTGRES_DB", databaseName);
		withEnv("POSTGRES_USER", username);
		withEnv("POSTGRES_PASSWORD", password);
		withCommand("/bin/bash", "-c", "mkdir -pv /docker-entrypoint-initdb.d/ && echo 'create schema if not exists keycloak;' > /docker-entrypoint-initdb.d/init.sql && docker-entrypoint.sh postgres");
	}

	public String getHostAndPort() {
		return getHost() + ":" + getMappedPort(PostgresSupabaseContainer.POSTGRESQL_PORT);
	}
}

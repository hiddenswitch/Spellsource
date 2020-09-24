package com.hiddenswitch.containers;

import org.testcontainers.containers.GenericContainer;

public class PostgresSupabaseContainer extends GenericContainer<PostgresSupabaseContainer> {

	public static final int POSTGRESQL_PORT = 5432;

	public PostgresSupabaseContainer(String username, String password, String databaseName) {
		super("supabase/postgres");
		withExposedPorts(POSTGRESQL_PORT);
		withEnv("POSTGRES_DB", databaseName);
		withEnv("POSTGRES_USER", username);
		withEnv("POSTGRES_PASSWORD", password);
		withCommand("postgres", "-c", "wal_level=logical");
	}

	public String getHostAndPort() {
		return getHost() + ":" + getMappedPort(PostgresSupabaseContainer.POSTGRESQL_PORT);
	}
}

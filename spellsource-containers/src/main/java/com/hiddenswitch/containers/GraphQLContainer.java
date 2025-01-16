package com.hiddenswitch.containers;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;

public class GraphQLContainer extends GenericContainer<GraphQLContainer> {

	public static final int GRAPHQL_PORT = 5678;

	public GraphQLContainer() {
		super("node:22");
		addEnv("NODE_ENV", "development");
		addEnv("GRAPHILE_ENV", "development");
		addEnv("SLEEP", "30"); // Allow the full pg schema to be setup before postgraphile introspection
		addExposedPort(GRAPHQL_PORT);
		addFileSystemBind("..", "/spellsource", BindMode.READ_WRITE, SelinuxContext.NONE);
		setWorkingDirectory("/spellsource/spellsource-graphql");
		setCommand("yarn", "develop");
	}

	public GraphQLContainer withPostgres(String postgresHostPort, String databaseName, String username, String password) {
		addEnv("PG_HOST", postgresHostPort);
		addEnv("PG_DATABASE", databaseName);
		addEnv("PG_USER", username);
		addEnv("PG_PASSWORD", password);

		return this;
	}
	
	public GraphQLContainer withKeycloak(String keycloakHost, int keycloakPort) {
		addEnv("KEYCLOAK_URL", "http://" + keycloakHost + ":" + keycloakPort);

		return this;
	}
}

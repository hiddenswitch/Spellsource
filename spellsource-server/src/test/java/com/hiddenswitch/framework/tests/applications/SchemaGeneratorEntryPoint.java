package com.hiddenswitch.framework.tests.applications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Starts the full server stack (Postgres, Keycloak, Redis, GraphQL containers + Java server),
 * waits for the stitched schema to be generated at spellsource-graphql/schema.graphql,
 * then shuts down and exits.
 * <p>
 * Used by the Gradle {@code generateSchema} task.
 */
public class SchemaGeneratorEntryPoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaGeneratorEntryPoint.class);

	public static void main(String[] args) throws Exception {
		var schemaFile = new File("../spellsource-graphql/schema.graphql");
		var lastModified = schemaFile.exists() ? schemaFile.lastModified() : 0L;

		LOGGER.info("Starting server stack for schema generation...");
		var application = new StandaloneApplication();
		var vertxFuture = application.deploy();
		var vertx = vertxFuture.toCompletionStage().toCompletableFuture().join();
		LOGGER.info("Server deployed successfully");

		LOGGER.info("Waiting for schema.graphql to be generated...");
		var maxAttempts = 120;
		for (int i = 0; i < maxAttempts; i++) {
			if (schemaFile.exists() && schemaFile.lastModified() > lastModified && schemaFile.length() > 0) {
				LOGGER.info("Schema generated successfully at {}", schemaFile.getAbsolutePath());
				break;
			}

			if (i == maxAttempts - 1) {
				LOGGER.error("Timed out waiting for schema generation after {} seconds", maxAttempts * 2);
				vertx.close().toCompletionStage().toCompletableFuture().join();
				System.exit(1);
			}
			Thread.sleep(2000);
		}

		LOGGER.info("Shutting down...");
		vertx.close().toCompletionStage().toCompletableFuture().join();

		// Testcontainers with reuse=true won't stop on their own, force exit
		System.exit(0);
	}
}
